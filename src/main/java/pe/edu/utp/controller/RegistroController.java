package pe.edu.utp.controller;

import javax.servlet.http.*;
import pe.edu.utp.service.PacienteService;

public class RegistroController extends ErpServlet {
    @Override protected void get(HttpServletRequest r,HttpServletResponse s) throws Exception { view(r,s,"registro"); }
    @Override protected void post(HttpServletRequest r,HttpServletResponse s) throws Exception {
        new PacienteService(dao).registrar(null,persona(r));
        ok(r,s,"/login","Registro recibido. Secretaría debe validar sus datos antes de ingresar y reservar.");
    }
}
