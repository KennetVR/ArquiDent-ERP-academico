package pe.edu.utp.controller;

import javax.servlet.http.*;
import pe.edu.utp.service.ReglaNegocio;

public class LogoutController extends ErpServlet {
    @Override protected void get(HttpServletRequest r,HttpServletResponse s) { throw new ReglaNegocio(405,"Use el botón Cerrar sesión."); }
    @Override protected void post(HttpServletRequest r,HttpServletResponse s) throws Exception {
        r.getSession().invalidate(); s.setStatus(303); s.setHeader("Location",r.getContextPath()+"/login");
    }
}
