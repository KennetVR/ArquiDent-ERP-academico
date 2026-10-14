package pe.edu.utp.filter;

import java.io.IOException;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;
import pe.edu.utp.dao.ErpDAO;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.service.PacienteService;
import pe.edu.utp.security.*;

public class ErpSecurityFilter implements Filter {
    private static final Set<String> PUBLICOS=Set.of("/login","/registro","/inicio","/servicios","/nosotros","/faq");
    private static final Set<String> PRIVADOS=Set.of("/pacientes","/recursos","/citas","/caja","/logout");
    private static final Set<String> ALIAS=Set.of("/dashboard-admin","/dashboard-secretaria","/dashboard-paciente","/dashboard-odontologo","/reservar-cita","/mis-citas","/editar-perfil");
    private static final Set<String> LOGOS=Set.of("/images/arquident-logo.png","/images/arquident-isotipo.png");
    private ServletContext ctx;
    @Override public void init(FilterConfig cfg) { ctx=cfg.getServletContext(); }
    @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain) throws IOException,ServletException {
        HttpServletRequest r=(HttpServletRequest)req; HttpServletResponse s=(HttpServletResponse)res;
        r.setCharacterEncoding("UTF-8"); s.setCharacterEncoding("UTF-8");
        s.setHeader("X-Content-Type-Options","nosniff"); s.setHeader("X-Frame-Options","DENY");
        s.setHeader("Referrer-Policy","same-origin");
        String path=r.getServletPath();
        boolean fuente=path.startsWith("/assets/") && path.endsWith(".woff2");
        boolean estilo=path.startsWith("/css/") || (path.startsWith("/assets/") && path.endsWith(".css"));
        if((estilo || fuente || LOGOS.contains(path)) && "GET".equals(r.getMethod())) {
            try (java.io.InputStream in=ctx.getResourceAsStream(path)) {
                if(in==null) { s.sendError(404); return; }
                s.setContentType(LOGOS.contains(path)?"image/png":fuente?"font/woff2":"text/css; charset=UTF-8");
                try (java.io.OutputStream out=s.getOutputStream()) { in.transferTo(out); }
            }
            return;
        }
        s.setHeader("Cache-Control","no-store");
        if(path.isEmpty()||"/".equals(path)||"/index.html".equals(path)) { s.sendRedirect(r.getContextPath()+"/inicio"); return; }
        if(ALIAS.contains(path) && "GET".equals(r.getMethod())) { s.sendRedirect(r.getContextPath()+"/citas"); return; }
        if(!PUBLICOS.contains(path)&&!PRIVADOS.contains(path)) { s.sendError(404); return; }
        if(!"GET".equals(r.getMethod())&&!"POST".equals(r.getMethod())) { s.sendError(405); return; }
        if("POST".equals(r.getMethod()) && Set.of("/inicio","/servicios","/nosotros","/faq").contains(path)) { s.sendError(405); return; }
        HttpSession session=r.getSession();
        if(session.getAttribute("csrf")==null) session.setAttribute("csrf",Csrf.nuevo());
        if("POST".equals(r.getMethod()) && !Csrf.valido((String)session.getAttribute("csrf"),r.getParameter("csrf"))) {
            s.setStatus(403); s.setContentType("text/plain;charset=UTF-8"); s.getWriter().write("Formulario vencido o token CSRF inválido. Recargue la página."); return;
        }
        Usuario u=(Usuario)session.getAttribute("usuario");
        if(u!=null) {
            try { u=new PacienteService(new ErpDAO()).vigente(u.getIdUsuario()); }
            catch(Exception e) { s.sendError(503,"Base de datos no disponible."); return; }
            if(u==null) { session.invalidate(); s.sendRedirect(r.getContextPath()+"/login"); return; }
            session.setAttribute("usuario",u); session.setAttribute("rolUsuario",u.getNombreRol());
        }
        if(PRIVADOS.contains(path) && u==null) {
            if("GET".equals(r.getMethod())) s.sendRedirect(r.getContextPath()+"/login"); else s.sendError(401);
            return;
        }
        if(("/recursos".equals(path)&&!Permisos.rol(u,"administrador")) || ("/caja".equals(path)&&!Permisos.caja(u))) { s.sendError(403); return; }
        chain.doFilter(r,s);
    }
}
