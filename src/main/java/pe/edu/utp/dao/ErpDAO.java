package pe.edu.utp.dao;

import java.sql.*;
import java.util.*;
import pe.edu.utp.config.Conexion;

/** DAO JDBC común. Las sentencias y fragmentos son constantes del backend; los valores son parámetros. */
public class ErpDAO {
    @FunctionalInterface public interface Connections { Connection open() throws SQLException; }
    @FunctionalInterface public interface Work<T> { T run(Connection c) throws SQLException; }
    private final Connections connections;
    public ErpDAO() { this(Conexion::getConection); }
    public ErpDAO(Connections connections) { this.connections = connections; }

    public <T> T read(Work<T> work) throws SQLException {
        try (Connection c = connections.open()) { return work.run(c); }
    }
    public <T> T transaction(Work<T> work) throws SQLException {
        try (Connection c = connections.open()) {
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            c.setAutoCommit(false);
            try { T result = work.run(c); c.commit(); return result; }
            catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public static List<Map<String, Object>> rows(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = statement(c, sql, params); ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> result = new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) row.put(meta.getColumnLabel(i), rs.getObject(i));
                result.add(row);
            }
            return result;
        }
    }
    public static Map<String, Object> one(Connection c, String sql, Object... params) throws SQLException {
        List<Map<String, Object>> r = rows(c, sql, params);
        return r.isEmpty() ? null : r.get(0);
    }
    public static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = statement(c, sql, params)) { return ps.executeUpdate(); }
    }
    public static int insert(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            if (ps.executeUpdate() != 1) throw new SQLException("No se insertó la fila.");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("No se obtuvo la clave.");
                return rs.getInt(1);
            }
        }
    }
    private static PreparedStatement statement(Connection c, String sql, Object[] params) throws SQLException {
        PreparedStatement ps = c.prepareStatement(sql);
        try { bind(ps, params); return ps; } catch (SQLException e) { ps.close(); throw e; }
    }
    private static void bind(PreparedStatement ps, Object[] values) throws SQLException {
        ps.setQueryTimeout(10);
        for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
    }
    public static int number(Map<String, Object> row, String name) { return ((Number) row.get(name)).intValue(); }
    public static boolean bool(Map<String, Object> row, String name) {
        Object v = row.get(name); return Boolean.TRUE.equals(v) || (v instanceof Number && ((Number) v).intValue() == 1);
    }
}
