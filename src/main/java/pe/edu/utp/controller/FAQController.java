package pe.edu.utp.controller;

import javax.servlet.http.*;
import static pe.edu.utp.dao.ErpDAO.*;

/** Ayuda pública. Solo lectura. */
public class FAQController extends ErpServlet {
    @Override protected void get(HttpServletRequest r, HttpServletResponse s) throws Exception {
        r.setAttribute("faqs", dao.read(c ->
                rows(c, "SELECT pregunta,respuesta FROM faq ORDER BY id_pregunta")));
        r.getRequestDispatcher("/WEB-INF/views/faq.jsp").forward(r, s);
    }
}
