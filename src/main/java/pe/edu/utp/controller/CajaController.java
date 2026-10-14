package pe.edu.utp.controller;

import javax.servlet.http.*;
import java.time.LocalDate;
import pe.edu.utp.service.*;
import pe.edu.utp.sunat.*;

public class CajaController extends ErpServlet {
    private final SunatClient sunat=new SunatClient(); // Caché de token por aplicación, nunca en sesión/JSP.
    private final FacturacionDirectaClient facturacion=new FacturacionDirectaClient();
    private final BoletaPdfService boletas=new BoletaPdfService(dao);
    @Override protected void get(HttpServletRequest r,HttpServletResponse s) throws Exception {
        if ("pdf".equals(r.getParameter("accion"))) {
            byte[] logo;
            try (java.io.InputStream in=getServletContext().getResourceAsStream("/images/arquident-logo.png")) {
                if(in==null) throw new IllegalStateException("Logo no disponible.");
                logo=in.readAllBytes();
            }
            BoletaPdfService.Pdf pdf=boletas.descargar(actor(r),id(r,"emision"),logo);
            s.setContentType("application/pdf");
            s.setHeader("Content-Disposition","inline; filename=\""+pdf.nombre+"\"");
            s.setContentLength(pdf.contenido.length); s.getOutputStream().write(pdf.contenido);
            return;
        }
        LocalDate hoy=LocalDate.now(Validar.LIMA);
        LocalDate desde=r.getParameter("desde")==null?hoy.withDayOfMonth(1):Validar.fecha(r.getParameter("desde"));
        LocalDate hasta=r.getParameter("hasta")==null?hoy.plusMonths(1):Validar.fecha(r.getParameter("hasta"));
        CajaService caja=new CajaService(dao,facturacion); caja.resumen(actor(r),desde,hasta).forEach(r::setAttribute);
        r.setAttribute("desde",desde); r.setAttribute("hasta",hasta); r.setAttribute("sunatConfigurado",sunat.configurado());
        r.setAttribute("emisionConfigurada",facturacion.configurado());
        int id=optionalId(r,"comprobante"); if(id>0) r.setAttribute("consultas",caja.consultas(actor(r),id));
        view(r,s,"caja");
    }
    @Override protected void post(HttpServletRequest r,HttpServletResponse s) throws Exception {
        CajaService caja=new CajaService(dao,facturacion); String a=r.getParameter("accion"),msg="Operación registrada.";
        if("cobrar".equals(a)) msg=caja.cobrar(actor(r),id(r,"cita"),r.getParameter("metodo"));
        else if("comprobante".equals(a)) caja.registrarComprobante(actor(r),id(r,"pago"),new Comprobante(r.getParameter("ruc"),r.getParameter("tipo"),r.getParameter("serie"),id(r,"numero"),Validar.fecha(r.getParameter("fecha")),Validar.dinero(r.getParameter("monto"))));
        else if("sunat".equals(a)) msg=caja.consultar(actor(r),id(r,"id"),sunat).mensaje;
        else throw new ReglaNegocio("Acción no reconocida.");
        ok(r,s,"/caja",msg);
    }
}
