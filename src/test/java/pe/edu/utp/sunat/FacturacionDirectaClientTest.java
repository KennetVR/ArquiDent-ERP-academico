package pe.edu.utp.sunat;

import com.google.gson.*;
import java.math.BigDecimal;
import java.net.http.HttpTimeoutException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

/** Transporte falso solo en pruebas unitarias; la aplicación utiliza HTTP real. */
public class FacturacionDirectaClientTest {
    private EmisionDemo documento() {
        return new EmisionDemo("BA26",100001,LocalDateTime.of(2026,10,14,10,15),12,1,
                "Paciente Demostración","40000005","Jr. Demo 400","Consulta General",new BigDecimal("50.00"));
    }
    private FacturacionDirectaClient cliente(int status,String body) {
        return new FacturacionDirectaClient(true,"BA26",r -> {
            assertEquals("POST",r.method());
            assertEquals(FacturacionDirectaClient.ENDPOINT,r.uri().toString());
            assertFalse(r.headers().firstValue("Authorization").isPresent());
            assertTrue(r.headers().firstValue("Content-Type").get().contains("application/json"));
            return new FacturacionDirectaClient.Reply(status,body);
        });
    }
    @Test public void payloadDemoDniSolesYTotalConIgv() {
        EmisionDemo d=documento();
        JsonObject p=new Gson().toJsonTree(d.payload()).getAsJsonObject();
        assertEquals("0",p.getAsJsonObject("empresa").get("modo").getAsString());
        assertEquals(EmisionDemo.RUC,p.getAsJsonObject("empresa").get("ruc").getAsString());
        assertEquals("1",p.getAsJsonObject("cliente").get("codigo_tipo_entidad").getAsString());
        assertEquals("1",p.getAsJsonObject("venta").get("moneda_id").getAsString());
        assertEquals("03",p.getAsJsonObject("venta").get("tipo_documento_codigo").getAsString());
        assertEquals(new BigDecimal("42.37"),d.gravada);
        assertEquals(new BigDecimal("7.63"),d.igv);
        assertEquals(d.monto,d.gravada.add(d.igv));
        assertEquals("Consulta General",p.getAsJsonArray("items").get(0).getAsJsonObject().get("producto").getAsString());
    }
    @Test public void aceptacionRequiereCodigoCeroYErrorFalse() {
        String body="{\"data\":{\"error\":\"false\",\"respuesta_sunat_codigo\":\"0\",\"respuesta_sunat_descripcion\":\"La Boleta ha sido aceptada\","
                +"\"ruta_pdf\":\"http://facturaciondirecta.com/API_SUNAT/files/facturacion_electronica/PDF/20604051984-03-BA26-100001.pdf\"}}";
        ResultadoEmision r=cliente(200,body).emitir(documento());
        assertEquals("ACEPTADO_DEMO",r.estado);
        assertEquals("0",r.codigo);
        assertTrue(r.pdf.startsWith("https://facturaciondirecta.com/"));
    }
    @Test public void http200ConErrorNoEsAceptacion() {
        ResultadoEmision r=cliente(200,"{\"data\":{\"error\":\"true\",\"respuesta_sunat_codigo\":\"2335\",\"respuesta_sunat_descripcion\":\"Documento rechazado\"}}").emitir(documento());
        assertEquals("RECHAZADO",r.estado); assertEquals(Integer.valueOf(200),r.httpStatus); assertNull(r.pdf);
        assertEquals("RECHAZADO",cliente(200,"{\"data\":{\"error\":false,\"respuesta_sunat_codigo\":\"2335\",\"respuesta_sunat_descripcion\":\"Rechazo\"}}").emitir(documento()).estado);
    }
    @Test public void htmlOContratoIncompletoNoEsExito() {
        for (String body:new String[]{"<html>Error PHP</html>","{}","{\"data\":{\"error\":false}}","{\"data\":{\"error\":\"quizas\",\"respuesta_sunat_codigo\":\"0\",\"respuesta_sunat_descripcion\":\"OK\"}}"})
            assertEquals("RESPUESTA_INVALIDA",cliente(200,body).emitir(documento()).estado);
    }
    @Test public void timeoutNoReintentaNiAfirmaRechazo() {
        AtomicInteger n=new AtomicInteger();
        FacturacionDirectaClient c=new FacturacionDirectaClient(true,"BA26",r -> { n.incrementAndGet(); throw new HttpTimeoutException("test"); });
        assertEquals("NO_CONFIRMADO",c.emitir(documento()).estado); assertEquals(1,n.get());
    }
    @Test public void httpFallidoYDesactivado() {
        assertEquals("ERROR_HTTP",cliente(503,"unavailable").emitir(documento()).estado);
        FacturacionDirectaClient c=new FacturacionDirectaClient(false,"BA26",r -> { throw new AssertionError("No debe consumir red"); });
        assertEquals("SIN_CONFIGURAR",c.emitir(documento()).estado);
    }
    @Test public void enlacesNoPuedenApuntarAOtroDominioODocumento() {
        String body="{\"data\":{\"error\":false,\"respuesta_sunat_codigo\":\"0\",\"respuesta_sunat_descripcion\":\"Aceptada\","
                +"\"ruta_pdf\":\"javascript:alert(1)\",\"ruta_xml\":\"https://otro.example/documento.xml\","
                +"\"ruta_cdr\":\"https://facturaciondirecta.com/API_SUNAT/files/facturacion_electronica/CDR/R-20604051984-03-BA26-999.xml\"}}";
        ResultadoEmision r=cliente(200,body).emitir(documento());
        assertNull(r.pdf); assertNull(r.xml); assertNull(r.cdr);
    }
}
