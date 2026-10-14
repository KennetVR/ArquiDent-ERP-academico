package pe.edu.utp.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.http.*;
import pe.edu.utp.dao.ErpDAO;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.service.*;

/** Manejo uniforme de errores; no se revelan SQL, credenciales ni trazas al navegador. */
public abstract class ErpServlet extends HttpServlet {
    protected final ErpDAO dao=new ErpDAO();
    protected Usuario actor(HttpServletRequest r) { return (Usuario)r.getSession().getAttribute("usuario"); }
    protected abstract void get(HttpServletRequest r,HttpServletResponse s) throws Exception;
    protected void post(HttpServletRequest r,HttpServletResponse s) throws Exception { throw new ReglaNegocio(405,"Método no permitido."); }
    @Override protected final void service(HttpServletRequest r,HttpServletResponse s) throws ServletException,IOException {
        try {
            if ("GET".equals(r.getMethod())) get(r,s);
            else if ("POST".equals(r.getMethod())) post(r,s);
            else throw new ReglaNegocio(405,"Método no permitido.");
        } catch (ReglaNegocio e) { error(r,s,e.getStatus(),e.getMessage()); }
        catch (SQLException e) {
            boolean duplicate=e.getSQLState()!=null && e.getSQLState().startsWith("23");
            error(r,s,duplicate?409:503,duplicate?"Registro duplicado o relación inválida. Revise DNI, correo, horario o comprobante.":"No se completó la operación en la base de datos. Revise la conexión y la migración antes de reintentar.");
        } catch (IllegalStateException e) { error(r,s,503,"Servicio no disponible. Revise la configuración privada de la base de datos y la migración."); }
        catch (Exception e) {
            String ref=java.util.UUID.randomUUID().toString();
            getServletContext().log("ArquiDent error " + ref + " tipo=" + e.getClass().getSimpleName());
            error(r,s,500,"No se completó la operación. Referencia: "+ref);
        }
    }
    private void error(HttpServletRequest r,HttpServletResponse s,int status,String message) throws ServletException,IOException {
        s.setStatus(status); r.setAttribute("error",message); view(r,s,"error");
    }
    protected void view(HttpServletRequest r,HttpServletResponse s,String name) throws ServletException,IOException {
        r.getRequestDispatcher("/WEB-INF/views/erp/"+name+".jsp").forward(r,s);
    }
    protected void ok(HttpServletRequest r,HttpServletResponse s,String path,String message) throws IOException {
        r.getSession().setAttribute("mensaje",message); s.setStatus(303); s.setHeader("Location",r.getContextPath()+path);
    }
    protected int id(HttpServletRequest r,String name) { return Validar.id(r.getParameter(name)); }
    protected int optionalId(HttpServletRequest r,String name) { String v=r.getParameter(name); return v==null||v.isBlank()?0:Validar.id(v); }
    protected Usuario persona(HttpServletRequest r) {
        Usuario p=new Usuario(); p.setIdUsuario(optionalId(r,"id")); p.setNombre(r.getParameter("nombre"));
        p.setCorreo(r.getParameter("correo")); p.setContrasena(r.getParameter("contrasena")); p.setDni(r.getParameter("dni"));
        p.setTelefono(r.getParameter("telefono")); p.setDireccion(r.getParameter("direccion"));
        String fecha=r.getParameter("nacimiento"); if(fecha!=null&&!fecha.isBlank()) p.setFechaNacimiento(Validar.fecha(fecha));
        return p;
    }
}
