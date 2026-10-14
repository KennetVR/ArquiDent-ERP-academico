package pe.edu.utp.sunat;

import org.junit.Test;
import java.math.BigDecimal;
import java.net.http.HttpTimeoutException;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;

/** SUNAT simulada con transporte falso: nunca se presenta como consulta real. */
public class SunatClientTest {
    private static final String TOKEN = "{\"access_token\":\"tok123\",\"token_type\":\"Bearer\",\"expires_in\":3600}";
    private static final String OK = "{\"success\":true,\"message\":\"Operation success\",\"data\":"
            + "{\"estadoCp\":\"1\",\"estadoRuc\":\"00\",\"condDomiRuc\":\"00\",\"Observaciones\":[]}}";
    private Comprobante doc() {
        return new Comprobante("20100066603", "03", "B001", 7, LocalDate.of(2026, 9, 1), new BigDecimal("80.00"));
    }
    private SunatClient cliente(Queue<SunatClient.Reply> respuestas, List<String> urls, int[] llamadasToken) {
        SunatClient.Transport t = request -> {
            urls.add(request.uri().toString());
            if (request.uri().toString().contains("oauth2/token")) llamadasToken[0]++;
            SunatClient.Reply r = respuestas.poll();
            if (r == null) throw new AssertionError("Llamada HTTP no esperada: " + request.uri());
            if (r.body instanceof String && ((String) r.body).startsWith("THROW_TIMEOUT")) throw new HttpTimeoutException("t");
            return r;
        };
        return new SunatClient("id-demo", "secreto-demo", "20100066603", t, Clock.fixed(Instant.parse("2026-10-14T16:00:00Z"), ZoneOffset.UTC));
    }
    private SunatClient.Reply reply(int status, String body) { return new SunatClient.Reply(status, body); }

    @Test public void sinCredencialesNoConsulta() {
        SunatClient c = new SunatClient(null, null, null, req -> { throw new AssertionError("No debe llamar red"); },
                Clock.systemUTC());
        ResultadoSunat r = c.consultar(doc());
        assertEquals("SIN_CONFIGURAR", r.resultado);
        assertNull(r.httpStatus);
    }
    @Test public void aceptadoInterpretaCodigosDelManual() {
        Queue<SunatClient.Reply> q = new ArrayDeque<>(Arrays.asList(reply(200, TOKEN), reply(200, OK)));
        List<String> urls = new ArrayList<>(); int[] tok = {0};
        ResultadoSunat r = cliente(q, urls, tok).consultar(doc());
        assertEquals("ACEPTADO", r.resultado);
        assertEquals(Integer.valueOf(200), r.httpStatus);
        assertEquals("1", r.estadoCp);
        assertEquals("00", r.estadoRuc);
        assertTrue(urls.get(1).endsWith("/20100066603/validarcomprobante"));
        assertEquals(1, tok[0]);
    }
    @Test public void tokenExpiradoReintentaUnaVez() {
        Queue<SunatClient.Reply> q = new ArrayDeque<>(Arrays.asList(
                reply(200, TOKEN), reply(401, "{}"), reply(200, TOKEN), reply(200, OK)));
        int[] tok = {0};
        ResultadoSunat r = cliente(q, new ArrayList<>(), tok).consultar(doc());
        assertEquals("ACEPTADO", r.resultado);
        assertEquals(2, tok[0]); // Un solo reintento de autenticación.
    }
    @Test public void credencialesRechazadasSonErrorDeAutenticacion() {
        Queue<SunatClient.Reply> q = new ArrayDeque<>(Arrays.asList(reply(400, "{}")));
        ResultadoSunat r = cliente(q, new ArrayList<>(), new int[1]).consultar(doc());
        assertEquals("ERROR_AUTENTICACION", r.resultado);
    }
    @Test public void successFalseNoEsComprobanteAceptado() {
        Queue<SunatClient.Reply> q = new ArrayDeque<>(Arrays.asList(reply(200, TOKEN),
                reply(200, "{\"success\":false,\"message\":\"Datos inválidos\",\"errorCode\":\"0100\"}")));
        ResultadoSunat r = cliente(q, new ArrayList<>(), new int[1]).consultar(doc());
        assertEquals("ERROR_CONSULTA", r.resultado);
        assertEquals("0100", r.codigoError);
    }
    @Test public void tiempoAgotadoYRespuestaInvalida() {
        Queue<SunatClient.Reply> q1 = new ArrayDeque<>(Arrays.asList(reply(200, TOKEN), reply(200, "THROW_TIMEOUT")));
        assertEquals("TIEMPO_AGOTADO", cliente(q1, new ArrayList<>(), new int[1]).consultar(doc()).resultado);
        Queue<SunatClient.Reply> q2 = new ArrayDeque<>(Arrays.asList(reply(200, TOKEN), reply(200, "no-json")));
        assertEquals("RESPUESTA_INVALIDA", cliente(q2, new ArrayList<>(), new int[1]).consultar(doc()).resultado);
    }
    @Test public void tokenSeReutilizaHastaVencer() {
        Queue<SunatClient.Reply> q = new ArrayDeque<>(Arrays.asList(reply(200, TOKEN), reply(200, OK), reply(200, OK)));
        int[] tok = {0};
        SunatClient c = cliente(q, new ArrayList<>(), tok);
        c.consultar(doc());
        c.consultar(doc());
        assertEquals(1, tok[0]);
    }
}
