package pe.edu.utp.service;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;
import pe.edu.utp.dao.ErpDAO;
import static pe.edu.utp.dao.ErpDAO.*;
import pe.edu.utp.sunat.*;

public class EmisionService {
    private final ErpDAO dao;
    private final FacturacionDirectaClient client;
    public EmisionService(ErpDAO dao,FacturacionDirectaClient client) { this.dao=dao; this.client=client; }
    public static final class Envio {
        final int id; final EmisionDemo documento;
        Envio(int id,EmisionDemo documento) { this.id=id; this.documento=documento; }
    }
    /** Dentro de la MISMA transacción de cobro. Un pago, un envío y un correlativo persistente. */
    public Envio preparar(Connection c,int pago,LocalDateTime fecha) throws SQLException {
        if (!client.configurado()) return null;
        Map<String,Object> p=one(c,"SELECT p.monto,a.id_cita,s.id_servicio,s.nombre AS servicio,u.nombre,u.dni,u.direccion FROM pagos p JOIN citas a ON a.id_cita=p.id_cita JOIN usuarios u ON u.id_usuario=a.id_paciente JOIN servicios s ON s.id_servicio=a.id_servicio WHERE p.id_pago=?",pago);
        update(c,"INSERT IGNORE INTO series_cpe_demo(num_ruc,serie,ultimo_numero) VALUES(?,?,100000)",EmisionDemo.RUC,client.serie());
        Map<String,Object> serie=one(c,"SELECT ultimo_numero FROM series_cpe_demo WHERE num_ruc=? AND serie=? FOR UPDATE",EmisionDemo.RUC,client.serie());
        int numero=number(serie,"ultimo_numero")+1;
        EmisionDemo doc=new EmisionDemo(client.serie(),numero,fecha,number(p,"id_cita"),number(p,"id_servicio"),
                (String)p.get("nombre"),(String)p.get("dni"),(String)p.get("direccion"),(String)p.get("servicio"),(BigDecimal)p.get("monto"));
        update(c,"UPDATE series_cpe_demo SET ultimo_numero=? WHERE num_ruc=? AND serie=?",numero,EmisionDemo.RUC,doc.serie);
        int id=insert(c,"INSERT INTO emisiones_cpe(id_pago,num_ruc,serie,numero,creado_en,cliente_nombre,cliente_dni,cliente_direccion,servicio,monto,gravada,igv) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                pago,EmisionDemo.RUC,doc.serie,doc.numero,doc.fecha,doc.nombre,doc.dni,doc.direccion,doc.servicio,doc.monto,doc.gravada,doc.igv);
        return new Envio(id,doc);
    }
    /** Después del commit: sin conexiones ni bloqueos SQL durante la llamada HTTP. Sin reenvíos automáticos. */
    public String enviar(Envio envio) {
        if (envio==null) return "Emisión demo desactivada; el cobro quedó registrado.";
        ResultadoEmision r=client.emitir(envio.documento);
        try {
            dao.transaction(c -> {
                if (update(c,"UPDATE emisiones_cpe SET estado=?,respondido_en=?,http_status=?,codigo_sunat=?,mensaje=?,ruta_pdf=?,ruta_xml=?,ruta_cdr=?,codigo_hash=? WHERE id_emision=? AND estado='ENVIANDO'",
                        r.estado,LocalDateTime.now(Validar.LIMA),r.httpStatus,r.codigo,r.mensaje,r.pdf,r.xml,r.cdr,r.hash,envio.id)!=1)
                    throw new SQLException("No se guardó el resultado de emisión.");
                return null;
            });
        } catch (SQLException | IllegalStateException e) {
            return "El cobro está registrado. No se pudo guardar la respuesta de la boleta "+envio.documento.documento()+"; revise el proveedor antes de reenviar.";
        }
        if ("ACEPTADO_DEMO".equals(r.estado)) return "Boleta "+envio.documento.documento()+" aceptada.";
        return "Boleta "+envio.documento.documento()+": "+r.estado+". "+r.mensaje;
    }
}
