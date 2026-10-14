package pe.edu.utp.sunat;

import org.junit.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import pe.edu.utp.service.ReglaNegocio;
import static org.junit.Assert.*;

public class ComprobanteTest {
    private Comprobante valido() {
        return new Comprobante("20100066603", "01", "F001", 128, LocalDate.of(2026, 10, 1), new BigDecimal("150.00"));
    }
    @Test public void payloadRespetaManualSunat() {
        Map<String, Object> p = valido().payload();
        assertEquals("20100066603", p.get("numRuc"));
        assertEquals("01", p.get("codComp"));
        assertEquals("F001", p.get("numeroSerie"));
        assertEquals(128, p.get("numero"));
        assertEquals("01/10/2026", p.get("fechaEmision"));
        assertEquals(new BigDecimal("150.00"), p.get("monto"));
    }
    @Test public void rechazaDatosInvalidos() {
        try { new Comprobante("123", "01", "F001", 1, LocalDate.of(2026, 1, 1), new BigDecimal("10.00")); fail(); }
        catch (ReglaNegocio e) { }
        try { new Comprobante("20100066603", "07", "F001", 1, LocalDate.of(2026, 1, 1), new BigDecimal("10.00")); fail(); }
        catch (ReglaNegocio e) { } // Alcance reducido: solo factura y boleta.
        try { new Comprobante("20100066603", "01", "f001", 1, LocalDate.of(2026, 1, 1), new BigDecimal("10.00")); fail(); }
        catch (ReglaNegocio e) { }
        try { new Comprobante("20100066603", "01", "F001", 0, LocalDate.of(2026, 1, 1), new BigDecimal("10.00")); fail(); }
        catch (ReglaNegocio e) { }
        try { new Comprobante("20100066603", "01", "F001", 1, LocalDate.now().plusDays(1), new BigDecimal("10.00")); fail(); }
        catch (ReglaNegocio e) { }
    }
}
