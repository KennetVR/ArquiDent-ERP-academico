package pe.edu.utp.controller;

import javax.servlet.http.*;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.service.PacienteService;
import pe.edu.utp.security.Csrf;

public class LoginController extends ErpServlet {
    @Override protected void get(HttpServletRequest r,HttpServletResponse s) throws Exception { view(r,s,"login"); }
    @Override protected void post(HttpServletRequest r,HttpServletResponse s) throws Exception {
        Usuario u=new PacienteService(dao).autenticar(r.getParameter("correo"),r.getParameter("contrasena"));
        if(u==null) { s.setStatus(401); r.setAttribute("error","Credenciales inválidas o cuenta pendiente de validación. Las claves heredadas requieren ejecutar la migración de contraseñas."); view(r,s,"login"); return; }
        r.getSession().invalidate();
        HttpSession session=r.getSession(true); session.setAttribute("usuario",u); session.setAttribute("rolUsuario",u.getNombreRol());
        session.setAttribute("csrf",Csrf.nuevo());
        ok(r,s,"/citas","Sesión iniciada.");
    }
}
