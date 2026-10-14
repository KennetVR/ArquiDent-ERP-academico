package pe.edu.utp.sunat;

import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Emisión DEMO real vía Facturación Directa. Contrato verificado en docs/SUNAT.md. */
public class FacturacionDirectaClient {
    public static final String ENDPOINT="https://facturaciondirecta.com/API_SUNAT/post.php";
    private final boolean habilitado;
    private final String serie;
    private final Transport transport;
    @FunctionalInterface interface Transport { Reply send(HttpRequest r) throws IOException,InterruptedException; }
    static final class Reply {
        final int status; final String body;
        Reply(int status,String body) { this.status=status; this.body=body; }
    }
    public FacturacionDirectaClient() {
        this(Boolean.parseBoolean(System.getenv("FD_DEMO_ENABLED")),
                System.getenv("FD_DEMO_SERIE")==null?"BA26":System.getenv("FD_DEMO_SERIE"),realTransport());
    }
    FacturacionDirectaClient(boolean habilitado,String serie,Transport transport) {
        this.habilitado=habilitado; this.serie=serie; this.transport=transport;
    }
    public boolean configurado() { return habilitado && serie!=null && serie.matches("B[A-Z0-9]{3}"); }
    public String serie() { return serie; }
    private static Transport realTransport() {
        HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();
        return request -> {
            HttpResponse<String> r=http.send(request,HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Reply(r.statusCode(),r.body());
        };
    }
    public ResultadoEmision emitir(EmisionDemo doc) {
        if (!configurado()) return ResultadoEmision.error("SIN_CONFIGURAR",null,"Emisión demo desactivada o serie inválida. Revise FD_DEMO_ENABLED y FD_DEMO_SERIE.");
        Integer status=null;
        try {
            Reply r=transport.send(HttpRequest.newBuilder(URI.create(ENDPOINT)).timeout(Duration.ofSeconds(35))
                    .header("Content-Type","application/json; charset=UTF-8").header("Accept","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(doc.payload()),StandardCharsets.UTF_8)).build());
            status=r.status;
            if (status<200 || status>=300) return ResultadoEmision.error("ERROR_HTTP",status,
                    "Facturación Directa respondió HTTP "+status+". El cobro está registrado; la emisión no está confirmada.");
            return interpretar(r,doc);
        } catch (HttpTimeoutException e) {
            return ResultadoEmision.error("NO_CONFIRMADO",status,"Tiempo de espera agotado. El cobro sigue registrado. Revise el documento en el proveedor antes de reenviar.");
        } catch (IOException e) {
            return ResultadoEmision.error("NO_CONFIRMADO",status,"No se obtuvo respuesta del proveedor. El cobro sigue registrado; no se confirma la emisión.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResultadoEmision.error("NO_CONFIRMADO",status,"Envío interrumpido. Revise el documento en el proveedor antes de reenviar.");
        } catch (RuntimeException e) {
            return ResultadoEmision.error("RESPUESTA_INVALIDA",status,"Respuesta no reconocida de Facturación Directa. El cobro sigue registrado; no se confirma la emisión.");
        }
    }
    private ResultadoEmision interpretar(Reply r,EmisionDemo doc) {
        JsonObject d=JsonParser.parseString(r.body).getAsJsonObject().getAsJsonObject("data");
        String error=texto(d,"error"),codigo=texto(d,"respuesta_sunat_codigo"),mensaje=texto(d,"respuesta_sunat_descripcion");
        if (!("true".equals(error)||"false".equals(error)) || codigo==null || codigo.isBlank() || codigo.length()>80 || mensaje==null || mensaje.isBlank())
            throw new IllegalArgumentException("Contrato no reconocido");
        boolean aceptado="false".equals(error) && "0".equals(codigo);
        return new ResultadoEmision(aceptado?"ACEPTADO_DEMO":"RECHAZADO",r.status,codigo,limpiar(mensaje,1000),
                aceptado?enlace(d,"ruta_pdf","PDF/"+doc.archivo()+".pdf"):null,
                aceptado?enlace(d,"ruta_xml","FIRMA/"+doc.archivo()+".xml"):null,
                aceptado?enlace(d,"ruta_cdr","CDR/R-"+doc.archivo()+".xml"):null,
                aceptado?limpiar(texto(d,"codigo_hash"),200):null);
    }
    private String texto(JsonObject j,String key) {
        if (j==null || !j.has(key) || !j.get(key).isJsonPrimitive()) return null;
        return j.get(key).getAsString();
    }
    private String limpiar(String s,int max) {
        if (s==null) return null;
        s=s.replace("MODDATOS","[omitido]").replaceAll("[\\p{Cntrl}]", " ");
        return s.substring(0,Math.min(s.length(),max));
    }
    private String enlace(JsonObject d,String key,String archivo) {
        String s=texto(d,key); if(s==null) return null;
        try {
            URI u=URI.create(s);
            String path="/API_SUNAT/files/facturacion_electronica/"+archivo;
            if (!("http".equals(u.getScheme())||"https".equals(u.getScheme())) || !"facturaciondirecta.com".equalsIgnoreCase(u.getHost())
                    || u.getPort()!=-1 || u.getUserInfo()!=null || u.getQuery()!=null || u.getFragment()!=null || !path.equals(u.getRawPath())) return null;
            return "https://facturaciondirecta.com"+path;
        } catch (IllegalArgumentException e) { return null; }
    }
}
