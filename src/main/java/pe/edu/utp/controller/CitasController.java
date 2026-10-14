package pe.edu.utp.controller;

import javax.servlet.http.*;
import pe.edu.utp.service.*;
import pe.edu.utp.security.Permisos;
import static pe.edu.utp.dao.ErpDAO.*;

public class CitasController extends ErpServlet {
    @Override protected void get(HttpServletRequest r,HttpServletResponse s) throws Exception {
        AgendaService agenda=new AgendaService(dao);
        r.setAttribute("citas",agenda.listar(actor(r),null));
        if(!Permisos.rol(actor(r),"odontologo")) {
            r.setAttribute("pacientes",new PacienteService(dao).listar(actor(r)));
            r.setAttribute("opciones",dao.read(c -> rows(c,"SELECT o.id_usuario AS doctor,o.nombre AS odontologo,s.id_servicio,s.nombre AS servicio,s.precio,s.duracion_minutos FROM odontologo_servicios a JOIN usuarios o ON o.id_usuario=a.id_odontologo JOIN servicios s ON s.id_servicio=a.id_servicio WHERE o.rol_id=3 AND o.activo=1 AND o.cuenta_validada=1 AND s.activo=1 ORDER BY o.nombre,s.nombre")));
            if("disponibilidad".equals(r.getParameter("accion"))) {
                int paciente=Permisos.rol(actor(r),"paciente")?actor(r).getIdUsuario():id(r,"paciente");
                int[] par=seleccion(r);
                r.setAttribute("horas",agenda.disponibles(actor(r),paciente,par[0],par[1],Validar.fecha(r.getParameter("fecha"))));
                r.setAttribute("pacienteElegido",paciente); r.setAttribute("doctorElegido",par[0]); r.setAttribute("servicioElegido",par[1]);
                r.setAttribute("fechaElegida",Validar.fecha(r.getParameter("fecha")));
            }
        }
        view(r,s,"citas");
    }
    private int[] seleccion(HttpServletRequest r) {
        String v=r.getParameter("opcion"); String[] p=v==null?new String[0]:v.split(":");
        if(p.length!=2) throw new ReglaNegocio("Seleccione un servicio y odontólogo asignados.");
        return new int[]{Validar.id(p[0]),Validar.id(p[1])};
    }
    @Override protected void post(HttpServletRequest r,HttpServletResponse s) throws Exception {
        AgendaService agenda=new AgendaService(dao); String a=r.getParameter("accion");
        if("reservar".equals(a)) {
            int paciente=Permisos.rol(actor(r),"paciente")?actor(r).getIdUsuario():id(r,"paciente");
            agenda.reservar(actor(r),paciente,id(r,"odontologo"),id(r,"servicio"),Validar.fecha(r.getParameter("fecha")),Validar.hora(r.getParameter("hora")),r.getParameter("motivo"));
        } else if("cancelar".equals(a)) agenda.cancelar(actor(r),id(r,"id"));
        else if("iniciar".equals(a)||"finalizar".equals(a)) agenda.atender(actor(r),id(r,"id"),"finalizar".equals(a),r.getParameter("observaciones"),r.getParameter("receta"),r.getParameter("derivacion"));
        else throw new ReglaNegocio("Acción no reconocida.");
        ok(r,s,"/citas","Operación guardada. Las reservas generan un pago pendiente; caja confirma al registrar el cobro.");
    }
}
