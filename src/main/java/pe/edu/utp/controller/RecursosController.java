package pe.edu.utp.controller;

import javax.servlet.http.*;
import pe.edu.utp.security.Permisos;
import pe.edu.utp.service.*;

public class RecursosController extends ErpServlet {
    @Override protected void get(HttpServletRequest r,HttpServletResponse s) throws Exception {
        Permisos.exigir(actor(r),"administrador");
        new RecursoService(dao).catalogos().forEach(r::setAttribute); view(r,s,"recursos");
    }
    @Override protected void post(HttpServletRequest r,HttpServletResponse s) throws Exception {
        RecursoService svc=new RecursoService(dao);
        String accion=r.getParameter("accion");
        if("odontologo".equals(accion)) svc.odontologo(actor(r),persona(r),"on".equals(r.getParameter("activo")));
        else if("servicio".equals(accion)) svc.servicio(actor(r),optionalId(r,"id"),r.getParameter("nombre"),r.getParameter("descripcion"),Validar.dinero(r.getParameter("precio")),id(r,"duracion"),"on".equals(r.getParameter("activo")),"on".equals(r.getParameter("previa")));
        else if("horario".equals(accion)) svc.horario(actor(r),id(r,"odontologo"),r.getParameter("dia"),Validar.hora(r.getParameter("inicio")),Validar.hora(r.getParameter("fin")));
        else if("quitarHorario".equals(accion)) svc.quitarHorario(actor(r),id(r,"id"));
        else if("asignar".equals(accion)||"quitarAsignacion".equals(accion)) svc.asignacion(actor(r),id(r,"odontologo"),id(r,"servicio"),"asignar".equals(accion));
        else throw new ReglaNegocio("Acción no reconocida.");
        ok(r,s,"/recursos","Configuración guardada. Las reservas usarán estos datos.");
    }
}
