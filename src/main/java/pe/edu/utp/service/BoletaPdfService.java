package pe.edu.utp.service;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import pe.edu.utp.dao.ErpDAO;
import static pe.edu.utp.dao.ErpDAO.*;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.pdf.LogoBoletaPdf;
import pe.edu.utp.security.Permisos;

public class BoletaPdfService {
    private static final int MAX_BYTES=5*1024*1024;
    private final ErpDAO dao;
    private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER).build();
    public BoletaPdfService(ErpDAO dao) { this.dao=dao; }
    public static final class Pdf {
        public final String nombre; public final byte[] contenido;
        Pdf(String nombre,byte[] contenido) { this.nombre=nombre; this.contenido=contenido; }
    }
    public Pdf descargar(Usuario actor,int emision,byte[] logo) throws SQLException {
        Permisos.exigir(actor,"secretaria","administrador");
        Map<String,Object> e=dao.read(c -> one(c,"SELECT num_ruc,cod_comp,serie,numero,estado,ruta_pdf FROM emisiones_cpe WHERE id_emision=?",emision));
        if(e==null) throw new ReglaNegocio(404,"Boleta no encontrada.");
        if(!"ACEPTADO_DEMO".equals(e.get("estado")) || e.get("ruta_pdf")==null)
            throw new ReglaNegocio(409,"Esta boleta aún no tiene un PDF disponible.");
        String serie=String.valueOf(e.get("serie")),ruc=String.valueOf(e.get("num_ruc")),tipo=String.valueOf(e.get("cod_comp"));
        if(!serie.matches("B[A-Z0-9]{3}") || !ruc.matches("[0-9]{11}") || !"03".equals(tipo))
            throw new ReglaNegocio(409,"Datos del documento inválidos.");
        String documento=serie+"-"+number(e,"numero");
        String url="https://facturaciondirecta.com/API_SUNAT/files/facturacion_electronica/PDF/"+ruc+"-"+tipo+"-"+documento+".pdf";
        if(!url.equals(e.get("ruta_pdf"))) throw new ReglaNegocio(409,"La ruta PDF no corresponde al documento.");
        try {
            HttpRequest request=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
                    .header("Accept","application/pdf").GET().build();
            HttpResponse<byte[]> respuesta=http.send(request,limitado());
            byte[] original=respuesta.body();
            if(respuesta.statusCode()!=200 || original.length<5 || original[0]!='%' || original[1]!='P'
                    || original[2]!='D' || original[3]!='F' || original[4]!='-') throw new IOException("Respuesta PDF no válida.");
            return new Pdf("boleta-"+documento+".pdf",LogoBoletaPdf.aplicar(original,logo));
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ReglaNegocio(503,"La descarga fue interrumpida. Intente abrir el PDF nuevamente.");
        } catch(IOException ex) {
            throw new ReglaNegocio(502,"No se pudo preparar el PDF de la boleta. Intente nuevamente en unos momentos.");
        }
    }
    // BodySubscriber acotado: el timeout de HttpRequest cubre también la recepción del cuerpo.
    private static HttpResponse.BodyHandler<byte[]> limitado() {
        return info -> new HttpResponse.BodySubscriber<byte[]>() {
            private final java.util.concurrent.CompletableFuture<byte[]> resultado=new java.util.concurrent.CompletableFuture<>();
            private final ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            private java.util.concurrent.Flow.Subscription subscription;
            public java.util.concurrent.CompletionStage<byte[]> getBody() { return resultado; }
            public void onSubscribe(java.util.concurrent.Flow.Subscription s) { subscription=s; s.request(1); }
            public void onNext(java.util.List<java.nio.ByteBuffer> buffers) {
                for(java.nio.ByteBuffer b:buffers) {
                    if(b.remaining()>MAX_BYTES-bytes.size()) {
                        subscription.cancel(); resultado.completeExceptionally(new IOException("PDF demasiado grande.")); return;
                    }
                    byte[] chunk=new byte[b.remaining()]; b.get(chunk); bytes.write(chunk,0,chunk.length);
                }
                subscription.request(1);
            }
            public void onError(Throwable e) { resultado.completeExceptionally(e); }
            public void onComplete() { resultado.complete(bytes.toByteArray()); }
        };
    }
}
