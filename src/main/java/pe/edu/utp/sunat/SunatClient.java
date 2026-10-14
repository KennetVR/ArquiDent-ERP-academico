package pe.edu.utp.sunat;

import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

/** Contrato: manual oficial SUNAT enlazado en docs/SUNAT.md. Solo backend, sin proveedor alternativo. */
public class SunatClient {
    private static final String SCOPE="https://api.sunat.gob.pe/v1/contribuyente/contribuyentes";
    private final String clientId,secret,rucConsulta;
    private final Transport transport;
    private final Clock clock;
    private String token;
    private Instant vence=Instant.EPOCH;
    private final Gson gson=new Gson();

    @FunctionalInterface interface Transport { Reply send(HttpRequest request) throws IOException,InterruptedException; }
    static final class Reply {
        final int status; final String body;
        Reply(int status,String body) { this.status=status; this.body=body; }
    }
    public SunatClient() {
        this(System.getenv("SUNAT_CLIENT_ID"),System.getenv("SUNAT_CLIENT_SECRET"),System.getenv("SUNAT_RUC_CONSULTA"),realTransport(),Clock.systemUTC());
    }
    // Constructor de paquete reservado a pruebas; el despliegue usa únicamente URLs oficiales fijas.
    SunatClient(String id,String secret,String ruc,Transport transport,Clock clock) {
        this.clientId=id; this.secret=secret; this.rucConsulta=ruc; this.transport=transport; this.clock=clock;
    }
    private static Transport realTransport() {
        HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();
        return request -> {
            HttpResponse<String> r=http.send(request,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Reply(r.statusCode(),r.body());
        };
    }
    public boolean configurado() {
        return clientId!=null && clientId.matches("[A-Za-z0-9-]{1,100}") && secret!=null && !secret.isBlank()
                && rucConsulta!=null && rucConsulta.matches("[0-9]{11}");
    }
    public ResultadoSunat consultar(Comprobante comprobante) {
        if (!configurado()) return ResultadoSunat.error("SIN_CONFIGURAR",null,"Consulta no realizada: configure privadamente SUNAT_CLIENT_ID, SUNAT_CLIENT_SECRET y SUNAT_RUC_CONSULTA.");
        try {
            String bearer=accessToken();
            Reply reply=validar(comprobante,bearer);
            if (reply.status==401) {
                invalidar(bearer);
                reply=validar(comprobante,accessToken()); // Un solo reintento de autenticación.
            }
            if (reply.status==401 || reply.status==403) return ResultadoSunat.error("ERROR_AUTENTICACION",reply.status,"SUNAT rechazó la autorización. Revise las credenciales y la habilitación del RUC consultante.");
            if (reply.status==429) return ResultadoSunat.error("NO_DISPONIBLE",429,"SUNAT limitó las consultas. Intente más tarde.");
            if (reply.status<200 || reply.status>=300) return ResultadoSunat.error("ERROR_HTTP",reply.status,"SUNAT no completó la consulta (HTTP "+reply.status+"). No se determinó la validez.");
            return interpretar(reply);
        } catch (TokenError e) {
            return ResultadoSunat.error("ERROR_AUTENTICACION",e.status,"No se obtuvo un token válido de SUNAT. Revise credenciales/habilitación o intente más tarde.");
        } catch (HttpTimeoutException e) {
            return ResultadoSunat.error("TIEMPO_AGOTADO",null,"SUNAT superó el tiempo de espera. Puede reintentar; no se determinó la validez.");
        } catch (IOException e) {
            return ResultadoSunat.error("NO_DISPONIBLE",null,"No fue posible conectar con SUNAT. Revise conectividad e intente más tarde.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResultadoSunat.error("INTERRUMPIDO",null,"Consulta interrumpida; no se determinó la validez.");
        } catch (RuntimeException e) {
            return ResultadoSunat.error("RESPUESTA_INVALIDA",null,"SUNAT devolvió una respuesta no reconocida; no se determinó la validez.");
        }
    }
    private Reply validar(Comprobante cp,String bearer) throws IOException,InterruptedException {
        return transport.send(HttpRequest.newBuilder(URI.create(SCOPE+"/"+rucConsulta+"/validarcomprobante"))
                .timeout(Duration.ofSeconds(15)).header("Authorization","Bearer "+bearer)
                .header("Content-Type","application/json").header("Accept","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(cp.payload()),StandardCharsets.UTF_8)).build());
    }
    private synchronized String accessToken() throws IOException,InterruptedException,TokenError {
        if (token!=null && clock.instant().isBefore(vence)) return token;
        String body="grant_type=client_credentials&scope="+encode(SCOPE)+"&client_id="+encode(clientId)+"&client_secret="+encode(secret);
        Reply r=transport.send(HttpRequest.newBuilder(URI.create("https://api-seguridad.sunat.gob.pe/v1/clientesextranet/"+clientId+"/oauth2/token/"))
                .timeout(Duration.ofSeconds(15)).header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build());
        if (r.status<200 || r.status>=300) throw new TokenError(r.status);
        try {
            JsonObject json=JsonParser.parseString(r.body).getAsJsonObject();
            String next=json.get("access_token").getAsString();
            long expires=json.get("expires_in").getAsLong();
            if (next.isBlank() || expires<=0 || !"Bearer".equalsIgnoreCase(json.get("token_type").getAsString())) throw new TokenError(r.status);
            token=next; vence=clock.instant().plusSeconds(expires-Math.min(30,expires/2)); return token;
        } catch (RuntimeException e) { throw new TokenError(r.status); }
    }
    private synchronized void invalidar(String anterior) { if (Objects.equals(token,anterior)) { token=null; vence=Instant.EPOCH; } }
    private ResultadoSunat interpretar(Reply r) {
        JsonObject j=JsonParser.parseString(r.body).getAsJsonObject();
        if (!j.has("success") || !j.get("success").isJsonPrimitive() || !j.getAsJsonPrimitive("success").isBoolean()) throw new IllegalArgumentException();
        if (!j.get("success").getAsBoolean()) return new ResultadoSunat("ERROR_CONSULTA",r.status,null,null,null,codigo(j,"errorCode",80),"SUNAT no pudo validar los datos del documento. Revise los campos y el código de error.",null);
        JsonObject d=j.getAsJsonObject("data");
        String cp=codigo(d,"estadoCp",8),ruc=codigo(d,"estadoRuc",8),dom=codigo(d,"condDomiRuc",8);
        if (cp==null || ruc==null || dom==null) throw new IllegalArgumentException();
        Map<String,String> estados=Map.of("0","NO_EXISTE","1","ACEPTADO","2","ANULADO","3","AUTORIZADO","4","NO_AUTORIZADO");
        String estado=estados.getOrDefault(cp,"ESTADO_DESCONOCIDO");
        JsonElement obs=d.has("observaciones")?d.get("observaciones"):d.get("Observaciones");
        String observaciones="";
        if (obs!=null && !obs.isJsonNull()) {
            if (!obs.isJsonArray()) throw new IllegalArgumentException();
            List<String> textos=new ArrayList<>();
            for (JsonElement v:obs.getAsJsonArray()) textos.add(v.getAsString());
            observaciones=limitar(String.join(" | ",textos),4000);
        }
        return new ResultadoSunat(estado,r.status,cp,ruc,dom,null,"Estado informado por SUNAT: "+estado+". No acredita el cobro.",observaciones);
    }
    private String codigo(JsonObject j,String key,int max) {
        if (j==null || !j.has(key) || j.get(key).isJsonNull()) return null;
        String s=j.get(key).getAsString();
        return s.matches("[A-Za-z0-9_.-]{1,"+max+"}")?limitar(s,max):null;
    }
    private String limitar(String s,int max) {
        // No se conserva el cuerpo HTTP completo ni el mensaje de autenticación.
        for(String privado:new String[]{secret,clientId,token}) if(privado!=null && !privado.isEmpty()) s=s.replace(privado,"[omitido]");
        s=s.replaceAll("[\\p{Cntrl}&&[^\\n\\t]]","");
        return s.substring(0,Math.min(max,s.length()));
    }
    private static String encode(String s) { return URLEncoder.encode(s,StandardCharsets.UTF_8); }
    private static final class TokenError extends Exception { final int status; TokenError(int s) { status=s; } }
}
