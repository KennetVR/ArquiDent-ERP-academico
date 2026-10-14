package pe.edu.utp.controller;

import javax.servlet.http.*;
import static pe.edu.utp.dao.ErpDAO.*;

/** Catálogo público. Solo lectura de servicios activos. */
public class ServiciosController extends ErpServlet {
    @Override protected void get(HttpServletRequest r, HttpServletResponse s) throws Exception {
        r.setAttribute("servicios", dao.read(c ->
                rows(c, "SELECT id_servicio,nombre,descripcion,precio,duracion_minutos FROM servicios WHERE activo=1 ORDER BY nombre")));
        r.getRequestDispatcher("/WEB-INF/views/servicios.jsp").forward(r, s);
    }
}
