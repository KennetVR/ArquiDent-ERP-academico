package pe.edu.utp.security;

import java.sql.*;

/** Migra claves heredadas en texto plano a PBKDF2 (una sola vía, sin revertir).
 *  Uso: mvn -q compile exec:java? No se añade plugin: se ejecuta con el classpath
 *  del WAR. Ver scripts/migrar-claves.ps1. Solo actualiza filas sin formato hash. */
public final class MigrarClaves {
    private MigrarClaves() { }
    public static void main(String[] args) throws Exception {
        String url = System.getenv("ARQ_DB_URL"), user = System.getenv("ARQ_DB_USER"), pass = System.getenv("ARQ_DB_PASSWORD");
        if (url == null || user == null || pass == null) throw new IllegalStateException("Configure ARQ_DB_URL/USER/PASSWORD.");
        Class.forName("com.mysql.cj.jdbc.Driver");
        int migradas = 0, yaHash = 0;
        try (Connection c = DriverManager.getConnection(url, user, pass);
             PreparedStatement sel = c.prepareStatement("SELECT id_usuario,contrasena FROM usuarios");
             ResultSet rs = sel.executeQuery()) {
            try (PreparedStatement upd = c.prepareStatement("UPDATE usuarios SET contrasena=? WHERE id_usuario=?")) {
                while (rs.next()) {
                    String actual = rs.getString(2);
                    if (Passwords.isHash(actual)) { yaHash++; continue; }
                    if (actual == null || actual.length() < 4 || actual.length() > 128) {
                        System.out.println("Omitido id=" + rs.getInt(1) + " (valor inválido; asigne clave manualmente).");
                        continue;
                    }
                    upd.setString(1, Passwords.hash(actual));
                    upd.setInt(2, rs.getInt(1));
                    upd.executeUpdate();
                    migradas++;
                }
            }
        }
        System.out.println("Claves migradas a hash: " + migradas + ". Ya con hash: " + yaHash + ".");
        System.out.println("Cambie las claves débiles heredadas y elimine este aviso.");
    }
}
