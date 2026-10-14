package pe.edu.utp.controller;

import javax.servlet.http.*;
import static pe.edu.utp.dao.ErpDAO.*;

/** Portada pública. Solo lectura del catálogo activo. */
public class InicioController extends ErpServlet {
    @Override protected void get(HttpServletRequest r, HttpServletResponse s) throws Exception {
        r.setAttribute("serviciosDestacados", dao.read(c ->
                rows(c, "SELECT id_servicio,nombre,descripcion,precio FROM servicios WHERE activo=1 ORDER BY id_servicio LIMIT 3")));
        r.getRequestDispatcher("/WEB-INF/views/inicio.jsp").forward(r, s);
    }
}
