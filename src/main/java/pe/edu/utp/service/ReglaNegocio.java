package pe.edu.utp.service;

public class ReglaNegocio extends RuntimeException {
    private final int status;
    public ReglaNegocio(String message) { this(400, message); }
    public ReglaNegocio(int status, String message) { super(message); this.status = status; }
    public int getStatus() { return status; }
}
