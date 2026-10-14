package pe.edu.utp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import pe.edu.utp.entity.Usuario;

public final class Validar {
    public static final ZoneId LIMA = ZoneId.of("America/Lima");
    private Validar() { }
    public static String texto(String s, String campo, int min, int max) {
        String v = s == null ? "" : s.trim();
        if (v.length() < min || v.length() > max || v.indexOf('\0') >= 0)
            throw new ReglaNegocio(campo + ": longitud permitida " + min + "–" + max + ".");
        return v;
    }
    public static int id(String s) {
        try { int n = Integer.parseInt(s); if (n > 0) return n; }
        catch (NumberFormatException ignored) { }
        throw new ReglaNegocio("Identificador inválido.");
    }
    public static LocalDate fecha(String s) {
        try { return LocalDate.parse(s); }
        catch (RuntimeException e) { throw new ReglaNegocio("Fecha inválida; use AAAA-MM-DD."); }
    }
    public static LocalTime hora(String s) {
        try {
            LocalTime t = LocalTime.parse(s);
            if (t.getSecond() == 0 && t.getNano() == 0) return t;
        } catch (RuntimeException ignored) { }
        throw new ReglaNegocio("Hora inválida; use HH:mm.");
    }
    public static BigDecimal dinero(String s) {
        try {
            if (s == null || !s.matches("[0-9]{1,8}(\\.[0-9]{1,2})?")) throw new IllegalArgumentException();
            BigDecimal n = new BigDecimal(s).setScale(2, RoundingMode.UNNECESSARY);
            if (n.signum() > 0) return n;
        } catch (IllegalArgumentException | ArithmeticException ignored) { }
        throw new ReglaNegocio("Importe inválido: positivo y hasta dos decimales.");
    }
    public static void rango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde) || hasta.isAfter(desde.plusYears(5)))
            throw new ReglaNegocio("Rango inválido; máximo cinco años.");
    }
    public static void clave(String s) {
        if (s == null || s.length() < 12 || s.length() > 128)
            throw new ReglaNegocio("La contraseña debe tener entre 12 y 128 caracteres.");
    }
    public static void persona(Usuario p) {
        p.setNombre(texto(p.getNombre(), "Nombre", 2, 100));
        String correo = texto(p.getCorreo(), "Correo", 5, 100).toLowerCase(java.util.Locale.ROOT);
        if (!correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new ReglaNegocio("Correo inválido.");
        p.setCorreo(correo);
        if (p.getDni() == null || !p.getDni().matches("[0-9]{8}")) throw new ReglaNegocio("El DNI debe contener 8 dígitos.");
        if (p.getTelefono() == null || !p.getTelefono().matches("[0-9+ -]{9,15}")) throw new ReglaNegocio("Teléfono inválido.");
        p.setDireccion(texto(p.getDireccion(), "Dirección", 0, 500));
        if (p.getFechaNacimiento() != null && (p.getFechaNacimiento().isAfter(LocalDate.now(LIMA))
                || p.getFechaNacimiento().isBefore(LocalDate.of(1900, 1, 1)))) throw new ReglaNegocio("Nacimiento inválido.");
    }
}
