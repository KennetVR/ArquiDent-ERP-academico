package pe.edu.utp.service;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;
import pe.edu.utp.dao.ErpDAO;
import static pe.edu.utp.dao.ErpDAO.*;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.security.Permisos;

public class AgendaService {
    private final ErpDAO dao;
    private final Clock clock;
    public AgendaService(ErpDAO dao) { this(dao, Clock.system(Validar.LIMA)); }
    public AgendaService(ErpDAO dao, Clock clock) { this.dao=dao; this.clock=clock; }

    public int reservar(Usuario actor,int paciente,int doctor,int servicio,LocalDate fecha,LocalTime hora,String motivo) throws SQLException {
        Permisos.exigir(actor,"paciente","secretaria","administrador");
        if (Permisos.rol(actor,"paciente") && paciente!=actor.getIdUsuario()) throw new ReglaNegocio(403,"Solo puede reservar para usted.");
        validarFecha(fecha,hora);
        String texto=Validar.texto(motivo,"Motivo",0,1000);
        return dao.transaction(c -> {
            // Serializa también las reservas del mismo paciente con distintos odontólogos.
            // Orden único de bloqueo para evitar interbloqueos entre reservas.
            rows(c,"SELECT id_usuario FROM usuarios WHERE id_usuario IN (?,?) ORDER BY id_usuario FOR UPDATE",paciente,doctor);
            if (one(c,"SELECT id_usuario FROM usuarios WHERE id_usuario=? AND rol_id=1 AND activo=1 AND cuenta_validada=1",paciente)==null)
                throw new ReglaNegocio("El paciente debe estar validado y activo.");
            Map<String,Object> s=servicioAsignado(c,doctor,servicio,true);
            int minutos=number(s,"duracion_minutos");
            if (bool(s,"requiere_consulta") && one(c,"SELECT id_cita FROM citas WHERE id_paciente=? AND estado='finalizada' LIMIT 1",paciente)==null)
                throw new ReglaNegocio("Este servicio requiere una atención previa finalizada.");
            if (!cubreHorario(c,doctor,fecha,hora,minutos)) throw new ReglaNegocio("Reserva fuera del horario laboral o no cabe la duración completa.");
            if (cruce(c,doctor,paciente,fecha,hora,minutos)) throw new ReglaNegocio(409,"El odontólogo o el paciente ya tiene una cita en ese intervalo.");
            BigDecimal precio=(BigDecimal)s.get("precio");
            if (precio==null || precio.signum()<=0) throw new ReglaNegocio("Configure un precio válido para el servicio.");
            int id=insert(c,"INSERT INTO citas(id_paciente,id_odontologo,id_servicio,fecha,hora,estado,duracion_minutos,motivo,erp_version) VALUES(?,?,?,?,?,'sin_pagar',?,?,1)",paciente,doctor,servicio,fecha,hora,minutos,texto);
            insert(c,"INSERT INTO pagos(id_cita,monto,estado_pago) VALUES(?,?,'pendiente')",id,precio);
            return id;
        });
    }
    private void validarFecha(LocalDate fecha,LocalTime hora) {
        LocalDateTime ahora=LocalDateTime.now(clock);
        if (fecha==null || hora==null || !LocalDateTime.of(fecha,hora).isAfter(ahora)
                || fecha.isAfter(ahora.toLocalDate().plusYears(1)) || hora.getSecond()!=0 || hora.getNano()!=0 || hora.getMinute()%15!=0)
            throw new ReglaNegocio("Seleccione un horario futuro, múltiplo de 15 minutos, dentro del próximo año.");
    }
    private Map<String,Object> servicioAsignado(Connection c,int doctor,int servicio,boolean lock) throws SQLException {
        if (one(c,"SELECT id_usuario FROM usuarios WHERE id_usuario=? AND rol_id=3 AND activo=1 AND cuenta_validada=1",doctor)==null)
            throw new ReglaNegocio("Odontólogo no disponible.");
        Map<String,Object> s=one(c,"SELECT * FROM servicios WHERE id_servicio=? AND activo=1"+(lock?" FOR UPDATE":""),servicio);
        if (s==null || one(c,"SELECT id_servicio FROM odontologo_servicios WHERE id_odontologo=? AND id_servicio=?",doctor,servicio)==null)
            throw new ReglaNegocio("El odontólogo no está asignado a ese servicio.");
        return s;
    }
    private boolean cubreHorario(Connection c,int doctor,LocalDate fecha,LocalTime hora,int minutos) throws SQLException {
        LocalDateTime fin=LocalDateTime.of(fecha,hora).plusMinutes(minutos);
        if (!fin.toLocalDate().equals(fecha)) return false;
        return one(c,"SELECT id_horario FROM horarios_odontologos WHERE id_odontologo=? AND dia_semana=? AND hora_inicio<=? AND hora_fin>=? LIMIT 1",
                doctor,RecursoService.DIAS.get(fecha.getDayOfWeek().getValue()-1),hora,fin.toLocalTime())!=null;
    }
    private boolean cruce(Connection c,int doctor,int paciente,LocalDate fecha,LocalTime hora,int minutos) throws SQLException {
        LocalDateTime inicio=LocalDateTime.of(fecha,hora),fin=inicio.plusMinutes(minutos);
        return one(c,"SELECT id_cita FROM citas WHERE (id_odontologo=? OR id_paciente=?) AND fecha=? AND estado<>'cancelada' AND TIMESTAMP(fecha,hora)<? AND TIMESTAMPADD(MINUTE,duracion_minutos,TIMESTAMP(fecha,hora))>? LIMIT 1",doctor,paciente,fecha,fin,inicio)!=null;
    }
    public List<String> disponibles(Usuario actor,int paciente,int doctor,int servicio,LocalDate fecha) throws SQLException {
        Permisos.exigir(actor,"paciente","secretaria","administrador");
        if (Permisos.rol(actor,"paciente") && paciente!=actor.getIdUsuario()) throw new ReglaNegocio(403,"Paciente no autorizado.");
        if (fecha.isBefore(LocalDate.now(clock)) || fecha.isAfter(LocalDate.now(clock).plusYears(1))) throw new ReglaNegocio("Fecha fuera del rango de reserva.");
        return dao.read(c -> {
            PacienteService.acceso(c,actor,paciente);
            int minutos=number(servicioAsignado(c,doctor,servicio,false),"duracion_minutos");
            List<String> horas=new ArrayList<>();
            for(int m=0;m<24*60;m+=15) {
                LocalTime h=LocalTime.of(m/60,m%60);
                if (LocalDateTime.of(fecha,h).isAfter(LocalDateTime.now(clock)) && cubreHorario(c,doctor,fecha,h,minutos) && !cruce(c,doctor,paciente,fecha,h,minutos)) horas.add(h.toString());
            }
            return horas;
        });
    }
    public List<Map<String,Object>> listar(Usuario actor,Integer paciente) throws SQLException {
        Permisos.exigir(actor,"paciente","odontologo","secretaria","administrador");
        return dao.read(c -> {
            List<Object> args=new ArrayList<>(); String where=" WHERE 1=1";
            if (paciente!=null) { PacienteService.acceso(c,actor,paciente); where+=" AND c.id_paciente=?"; args.add(paciente); }
            if (Permisos.rol(actor,"paciente")) { where+=" AND c.id_paciente=?"; args.add(actor.getIdUsuario()); }
            if (Permisos.rol(actor,"odontologo")) { where+=" AND c.id_odontologo=?"; args.add(actor.getIdUsuario()); }
            String clinico=Permisos.caja(actor)?"NULL AS observaciones,NULL AS receta,NULL AS derivacion":"c.observaciones,c.receta,c.derivacion";
            return rows(c,"SELECT c.id_cita,c.id_paciente,c.id_odontologo,c.fecha,c.hora,c.estado,c.duracion_minutos,c.hora_inicio_real,c.hora_fin_real,c.motivo,"+clinico+",u.nombre AS paciente,o.nombre AS odontologo,s.nombre AS servicio,p.id_pago,p.monto,p.estado_pago,p.metodo_pago,p.fecha_pago FROM citas c JOIN usuarios u ON u.id_usuario=c.id_paciente JOIN usuarios o ON o.id_usuario=c.id_odontologo JOIN servicios s ON s.id_servicio=c.id_servicio LEFT JOIN pagos p ON p.id_cita=c.id_cita"+where+" ORDER BY c.fecha DESC,c.hora DESC,c.id_cita DESC",args.toArray());
        });
    }
    static Map<String,Object> citaBloqueada(Connection c,Usuario actor,int id) throws SQLException {
        Map<String,Object> a=one(c,"SELECT * FROM citas WHERE id_cita=? FOR UPDATE",id);
        if (a==null) throw new ReglaNegocio(404,"Cita no encontrada.");
        Permisos.cita(actor,number(a,"id_paciente"),number(a,"id_odontologo")); return a;
    }
    public void cancelar(Usuario actor,int id) throws SQLException {
        Permisos.exigir(actor,"paciente","secretaria","administrador");
        dao.transaction(c -> {
            Map<String,Object> a=citaBloqueada(c,actor,id);
            if (!Arrays.asList("sin_pagar","derivada_sin_pagar","reservada").contains(a.get("estado")))
                throw new ReglaNegocio(409,"Solo se cancela una reserva sin cobro. La devolución de cobros queda fuera del prototipo.");
            Map<String,Object> p=one(c,"SELECT * FROM pagos WHERE id_cita=? FOR UPDATE",id);
            if (p==null || "pagado".equals(p.get("estado_pago"))) throw new ReglaNegocio(409,"Pago incompatible; requiere revisión de caja.");
            update(c,"UPDATE pagos SET estado_pago='cancelado' WHERE id_cita=?",id);
            update(c,"UPDATE citas SET estado='cancelada' WHERE id_cita=?",id); return null;
        });
    }
    public void atender(Usuario actor,int id,boolean finalizar,String observaciones,String receta,String derivacion) throws SQLException {
        Permisos.exigir(actor,"odontologo");
        String o=Validar.texto(observaciones,"Observaciones",finalizar?1:0,4000);
        String r=Validar.texto(receta,"Receta",0,4000),d=Validar.texto(derivacion,"Derivación",0,2000);
        dao.transaction(c -> {
            Map<String,Object> a=citaBloqueada(c,actor,id);
            String esperado=finalizar?"en_atencion":"confirmada";
            if (!esperado.equals(a.get("estado"))) throw new ReglaNegocio(409,"Transición inválida: se requiere estado "+esperado+".");
            Map<String,Object> pago=one(c,"SELECT estado_pago FROM pagos WHERE id_cita=? FOR UPDATE",id);
            if (pago==null || !"pagado".equals(pago.get("estado_pago"))) throw new ReglaNegocio(409,"La atención requiere un cobro registrado.");
            if (!finalizar && !LocalDate.parse(a.get("fecha").toString()).equals(LocalDate.now(clock))) throw new ReglaNegocio("La atención debe iniciarse el día de la cita.");
            if (finalizar) update(c,"UPDATE citas SET estado='finalizada',hora_fin_real=?,observaciones=?,receta=?,derivacion=? WHERE id_cita=?",LocalDateTime.now(clock),o,r,d,id);
            else update(c,"UPDATE citas SET estado='en_atencion',hora_inicio_real=? WHERE id_cita=?",LocalDateTime.now(clock),id);
            return null;
        });
    }
}
