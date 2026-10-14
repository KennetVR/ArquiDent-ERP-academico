package pe.edu.utp.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;
import pe.edu.utp.dao.ErpDAO;
import static pe.edu.utp.dao.ErpDAO.*;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.security.Permisos;
import pe.edu.utp.sunat.*;

public class CajaService {
    public static final List<String> METODOS=Arrays.asList("efectivo","tarjeta","transferencia","yape","plin");
    private final ErpDAO dao;
    private final EmisionService emision;
    public CajaService(ErpDAO dao) { this(dao,new FacturacionDirectaClient()); }
    public CajaService(ErpDAO dao,FacturacionDirectaClient client) { this.dao=dao; this.emision=new EmisionService(dao,client); }

    public String cobrar(Usuario actor,int cita,String metodo) throws SQLException {
        Permisos.exigir(actor,"secretaria","administrador");
        if (!METODOS.contains(metodo)) throw new ReglaNegocio("Método de pago inválido.");
        EmisionService.Envio envio=dao.transaction(c -> {
            Map<String,Object> a=AgendaService.citaBloqueada(c,actor,cita);
            if (!Arrays.asList("sin_pagar","derivada_sin_pagar").contains(a.get("estado"))) throw new ReglaNegocio(409,"La cita no admite un nuevo cobro.");
            if (LocalDate.parse(a.get("fecha").toString()).isBefore(LocalDate.now(Validar.LIMA))) throw new ReglaNegocio("No se confirma una cita pasada; revise el registro histórico.");
            Map<String,Object> p=one(c,"SELECT * FROM pagos WHERE id_cita=? FOR UPDATE",cita);
            if (p==null || !"pendiente".equals(p.get("estado_pago")) || ((BigDecimal)p.get("monto")).signum()<=0)
                throw new ReglaNegocio(409,"El pago no está pendiente o requiere revisión.");
            LocalDateTime fecha=LocalDateTime.now(Validar.LIMA);
            if (update(c,"UPDATE pagos SET estado_pago='pagado',metodo_pago=?,fecha_pago=?,cobrado_por=? WHERE id_cita=? AND estado_pago='pendiente'",metodo,fecha,actor.getIdUsuario(),cita)!=1) throw new SQLException("No se registró el cobro.");
            if (update(c,"UPDATE citas SET estado='confirmada' WHERE id_cita=?",cita)!=1) throw new SQLException("No se confirmó la cita.");
            return emision.preparar(c,number(p,"id_pago"),fecha);
        });
        return "Cobro registrado y cita confirmada. "+emision.enviar(envio);
    }
    public Map<String,Object> resumen(Usuario actor,LocalDate desde,LocalDate hasta) throws SQLException {
        Permisos.exigir(actor,"secretaria","administrador"); Validar.rango(desde,hasta);
        return dao.read(c -> {
            Map<String,Object> m=new HashMap<>();
            // Ingresos por fecha del cobro; sin joins con consultas tributarias (evita multiplicar montos).
            m.put("ingresos",one(c,"SELECT COALESCE(SUM(monto),0.00) AS total FROM pagos WHERE estado_pago='pagado' AND fecha_pago>=? AND fecha_pago<?",desde.atStartOfDay(),hasta.plusDays(1).atStartOfDay()).get("total"));
            m.put("cobros",rows(c,"SELECT p.*,c.fecha,c.hora,u.nombre AS paciente FROM pagos p JOIN citas c ON c.id_cita=p.id_cita JOIN usuarios u ON u.id_usuario=c.id_paciente WHERE p.estado_pago='pagado' AND p.fecha_pago>=? AND p.fecha_pago<? ORDER BY p.fecha_pago DESC",desde.atStartOfDay(),hasta.plusDays(1).atStartOfDay()));
            m.put("pendientes",rows(c,"SELECT p.*,c.fecha,c.hora,u.nombre AS paciente,s.nombre AS servicio FROM pagos p JOIN citas c ON c.id_cita=p.id_cita JOIN usuarios u ON u.id_usuario=c.id_paciente JOIN servicios s ON s.id_servicio=c.id_servicio WHERE p.estado_pago='pendiente' AND c.estado IN ('sin_pagar','derivada_sin_pagar') AND c.fecha BETWEEN ? AND ? ORDER BY c.fecha,c.hora",desde,hasta));
            m.put("comprobantes",rows(c,"SELECT cp.*,p.estado_pago,cs.resultado,cs.estado_cp,cs.estado_ruc,cs.condicion_domicilio,cs.consultado_en,cs.mensaje,cs.observaciones,cs.http_status,cs.codigo_error FROM comprobantes cp JOIN pagos p ON p.id_pago=cp.id_pago LEFT JOIN consultas_sunat cs ON cs.id_consulta=(SELECT MAX(x.id_consulta) FROM consultas_sunat x WHERE x.id_comprobante=cp.id_comprobante) WHERE cp.fecha_emision BETWEEN ? AND ? ORDER BY cp.id_comprobante DESC",desde,hasta));
            m.put("emisiones",rows(c,"SELECT e.*,p.id_cita FROM emisiones_cpe e JOIN pagos p ON p.id_pago=e.id_pago WHERE e.creado_en>=? AND e.creado_en<? ORDER BY e.id_emision DESC",desde.atStartOfDay(),hasta.plusDays(1).atStartOfDay()));
            m.put("metodos",METODOS); return m;
        });
    }
    public int registrarComprobante(Usuario actor,int pago,Comprobante cp) throws SQLException {
        Permisos.exigir(actor,"secretaria","administrador");
        return dao.transaction(c -> {
            Map<String,Object> p=one(c,"SELECT * FROM pagos WHERE id_pago=? FOR UPDATE",pago);
            if (p==null) throw new ReglaNegocio(404,"Pago no encontrado.");
            if (!"pagado".equals(p.get("estado_pago"))) throw new ReglaNegocio("Registre primero el cobro recibido.");
            if (cp.monto.compareTo((BigDecimal)p.get("monto"))!=0) throw new ReglaNegocio("En este prototipo el comprobante debe corresponder al importe completo del pago.");
            return insert(c,"INSERT INTO comprobantes(id_pago,num_ruc,cod_comp,serie,numero,fecha_emision,monto,registrado_por) VALUES(?,?,?,?,?,?,?,?)",pago,cp.ruc,cp.tipo,cp.serie,cp.numero,cp.fecha,cp.monto,actor.getIdUsuario());
        });
    }
    public ResultadoSunat consultar(Usuario actor,int id,SunatClient sunat) throws SQLException {
        Permisos.exigir(actor,"secretaria","administrador");
        Comprobante cp=dao.read(c -> {
            Map<String,Object> r=one(c,"SELECT * FROM comprobantes WHERE id_comprobante=?",id);
            if (r==null) throw new ReglaNegocio(404,"Comprobante no encontrado.");
            return new Comprobante((String)r.get("num_ruc"),(String)r.get("cod_comp"),(String)r.get("serie"),number(r,"numero"),LocalDate.parse(r.get("fecha_emision").toString()),(BigDecimal)r.get("monto"));
        });
        // No mantener bloqueos ni conexión SQL mientras se espera a SUNAT.
        ResultadoSunat r=sunat.consultar(cp);
        dao.transaction(c -> {
            insert(c,"INSERT INTO consultas_sunat(id_comprobante,consultado_por,resultado,http_status,estado_cp,estado_ruc,condicion_domicilio,codigo_error,mensaje,observaciones) VALUES(?,?,?,?,?,?,?,?,?,?)",id,actor.getIdUsuario(),r.resultado,r.httpStatus,r.estadoCp,r.estadoRuc,r.condicion,r.codigoError,r.mensaje,r.observaciones);
            return null;
        });
        return r;
    }
    public List<Map<String,Object>> consultas(Usuario actor,int comprobante) throws SQLException {
        Permisos.exigir(actor,"secretaria","administrador");
        return dao.read(c -> rows(c,"SELECT * FROM consultas_sunat WHERE id_comprobante=? ORDER BY id_consulta DESC",comprobante));
    }
}
