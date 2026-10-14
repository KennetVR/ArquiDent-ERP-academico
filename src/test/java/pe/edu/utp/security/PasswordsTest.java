package pe.edu.utp.security;

import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordsTest {
    @Test public void hashYVerificacion() {
        String hash = Passwords.hash("ClaveFicticia-2026");
        assertTrue(Passwords.isHash(hash));
        assertTrue(Passwords.verify("ClaveFicticia-2026", hash));
        assertFalse(Passwords.verify("otra-clave", hash));
    }
    @Test public void rechazaTextoPlanoHeredado() {
        assertFalse(Passwords.isHash("pass123"));
        assertFalse(Passwords.verify("pass123", "pass123"));
        assertFalse(Passwords.verify(null, Passwords.hash("x".repeat(20))));
    }
    @Test public void salesDistintasParaLaMismaClave() {
        assertNotEquals(Passwords.hash("MismaClave-1234"), Passwords.hash("MismaClave-1234"));
    }
    @Test public void interoperabilidadConSemillaDemo() {
        // Vector generado con hashlib.pbkdf2_hmac sha256, 600 000 iteraciones (cuenta ficticia de demostración).
        String demo = "pbkdf2-sha256$600000$PbnC2zyxlyt8uvg5y6X8VA==$uxEi+5Vga6aU6F28AU6w52I2z+yGIN/IneAYiiAJqi4=";
        assertTrue(Passwords.verify("AdminDemo-2026-01", demo));
        assertFalse(Passwords.verify("AdminDemo-2026-02", demo));
    }
}
