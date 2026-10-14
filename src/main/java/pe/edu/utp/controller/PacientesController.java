package pe.edu.utp.controller;

import javax.servlet.http.*;
import pe.edu.utp.service.*;

public class PacientesController extends ErpServlet {
    @Override protected void get(HttpServletRequest r,HttpServletResponse s) throws Exception {
        PacienteService pacientes=new PacienteService(dao);
        r.setAttribute("pacientes",pacientes.listar(actor(r)));
        int id=optionalId(r,"id");
        if(id>0) { r.setAttribute("ficha",pacientes.obtener(actor(r),id)); r.setAttribute("historial",new AgendaService(dao).listar(actor(r),id)); }
        view(r,s,"pacientes");
    }
    @Override protected void post(HttpServletRequest r,HttpServletResponse s) throws Exception {
        PacienteService pacientes=new PacienteService(dao);
        if("registrar".equals(r.getParameter("accion"))) pacientes.registrar(actor(r),persona(r));
        else if("actualizar".equals(r.getParameter("accion"))) pacientes.actualizar(actor(r),persona(r),"on".equals(r.getParameter("validado")));
        else throw new ReglaNegocio("Acción no reconocida.");
        ok(r,s,"/pacientes","Datos del paciente guardados.");
    }
}
