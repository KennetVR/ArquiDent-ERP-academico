package pe.edu.utp.controller;

import javax.servlet.http.*;

/** Página institucional sin acceso a datos. */
public class NosotrosController extends ErpServlet {
    @Override protected void get(HttpServletRequest r, HttpServletResponse s) throws Exception {
        r.getRequestDispatcher("/WEB-INF/views/nosotros.jsp").forward(r, s);
    }
}
