package pe.edu.utp.security;

import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Csrf {
    private Csrf() { }
    public static String nuevo() { byte[] b=new byte[32]; new SecureRandom().nextBytes(b); return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }
    public static boolean valido(String esperado,String recibido) {
        return esperado!=null && recibido!=null && MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),recibido.getBytes(StandardCharsets.UTF_8));
    }
}
