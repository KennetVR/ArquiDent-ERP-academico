package pe.edu.utp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Configuración compartida por los DAO. No imprime URL, usuario ni secretos. */
public final class Conexion {
    private Conexion() { }

    public static Connection getConection() {
        String url = System.getenv("ARQ_DB_URL");
        String user = System.getenv("ARQ_DB_USER");
        String password = System.getenv("ARQ_DB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new IllegalStateException("Configure ARQ_DB_URL, ARQ_DB_USER y ARQ_DB_PASSWORD en el servidor.");
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection c = DriverManager.getConnection(url, user, password);
            try (java.sql.Statement s = c.createStatement()) {
                s.execute("SET time_zone = '-05:00'");
            }
            return c;
        } catch (SQLException | ClassNotFoundException e) {
            throw new IllegalStateException("Base de datos no disponible. Revise la configuración privada y la migración.");
        }
    }

    public static void closeConnection(Connection c) {
        if (c != null) try { c.close(); } catch (SQLException ignored) { }
    }
}
