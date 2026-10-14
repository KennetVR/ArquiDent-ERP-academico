package pe.edu.utp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Usuario {
    private int idUsuario;
    private String nombre;
    private String correo;
    private String contrasena;
    private int rolId;
    private String telefono;
    private String direccion;
    private LocalDate fechaNacimiento;
    private LocalDateTime fechaRegistro;
    private String nombreRol; // Para joins
    
    // Nuevos campos para validación
    private String dni;
    private String fotoDniFrontal;
    private String fotoDniReverso;
    private boolean cuentaValidada;
    private LocalDateTime fechaSolicitudRegistro;

    // Constructores
    public Usuario() {}

    public Usuario(String nombre, String correo, String contrasena, int rolId, 
                   String telefono, String direccion, LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rolId = rolId;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
        this.cuentaValidada = false; // Por defecto no validada
    }

    // Getters y Setters existentes
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    // Nuevos getters y setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getFotoDniFrontal() { return fotoDniFrontal; }
    public void setFotoDniFrontal(String fotoDniFrontal) { this.fotoDniFrontal = fotoDniFrontal; }

    public String getFotoDniReverso() { return fotoDniReverso; }
    public void setFotoDniReverso(String fotoDniReverso) { this.fotoDniReverso = fotoDniReverso; }

    public boolean isCuentaValidada() { return cuentaValidada; }
    public void setCuentaValidada(boolean cuentaValidada) { this.cuentaValidada = cuentaValidada; }

    public LocalDateTime getFechaSolicitudRegistro() { return fechaSolicitudRegistro; }
    public void setFechaSolicitudRegistro(LocalDateTime fechaSolicitudRegistro) { this.fechaSolicitudRegistro = fechaSolicitudRegistro; }
}
