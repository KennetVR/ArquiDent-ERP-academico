package pe.edu.utp.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2-HMAC-SHA256: 600 000 iteraciones, sal aleatoria 128 bits, clave 256 bits. */
public final class Passwords {
    private static final int ITERATIONS = 600_000;
    private Passwords() { }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return "pbkdf2-sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
    }

    public static boolean isHash(String value) {
        return value != null && value.startsWith("pbkdf2-sha256$");
    }

    public static boolean verify(String password, String encoded) {
        if (password == null || password.length() > 128 || !isHash(encoded)) return false;
        try {
            String[] p = encoded.split("\\$");
            int iterations = Integer.parseInt(p[1]);
            if (p.length != 4 || iterations < 600_000 || iterations > 2_000_000) return false;
            return MessageDigest.isEqual(Base64.getDecoder().decode(p[3]),
                    derive(password, Base64.getDecoder().decode(p[2]), iterations));
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) { return false; }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (java.security.GeneralSecurityException e) { throw new IllegalStateException("Hash no disponible", e); }
        finally { spec.clearPassword(); Arrays.fill(chars, '\0'); }
    }
}
