package pe.edu.utp.service;

import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

public class ValidarTest {
    @Test public void dineroAceptaDosDecimales() {
        assertEquals(new BigDecimal("50.00"), Validar.dinero("50"));
        assertEquals(new BigDecimal("80.50"), Validar.dinero("80.50"));
    }
    @Test public void dineroRechazaFormatos() {
        for (String malo : new String[]{null, "", "0", "-5", "10.123", "1,000.00", "abc", "9999999999"}) {
            try { Validar.dinero(malo); fail("Aceptó: " + malo); }
            catch (ReglaNegocio e) { assertEquals(400, e.getStatus()); }
        }
    }
    @Test public void textoRespetaLimites() {
        assertEquals("abc", Validar.texto("  abc ", "Campo", 2, 5));
        try { Validar.texto("a", "Campo", 2, 5); fail(); } catch (ReglaNegocio e) { }
        try { Validar.texto("abcdef", "Campo", 2, 5); fail(); } catch (ReglaNegocio e) { }
    }
    @Test public void fechaYHoraEstrictas() {
        assertEquals("2026-10-20", Validar.fecha("2026-10-20").toString());
        assertEquals("10:15", Validar.hora("10:15").toString());
        try { Validar.fecha("20/10/2026"); fail(); } catch (ReglaNegocio e) { }
        try { Validar.hora("10:15:30"); fail(); } catch (ReglaNegocio e) { }
        try { Validar.hora("10-15"); fail(); } catch (ReglaNegocio e) { }
    }
    @Test public void claveMinimoDoce() {
        try { Validar.clave("corta123"); fail(); } catch (ReglaNegocio e) { }
        Validar.clave("ClaveLarga-2026");
    }
    @Test public void rangoMaximoCincoAnios() {
        Validar.rango(java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 1, 31));
        try { Validar.rango(java.time.LocalDate.of(2026, 2, 1), java.time.LocalDate.of(2026, 1, 1)); fail(); }
        catch (ReglaNegocio e) { }
    }
}
