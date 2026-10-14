package pe.edu.utp.sunat;

import java.sql.*;
import java.time.*;
import java.net.http.HttpTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.*;
import static org.junit.Assert.*;
import static pe.edu.utp.dao.ErpDAO.*;
import pe.edu.utp.dao.ErpDAO;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.service.*;

/** Integración MySQL opcional en esquema separado arquident_cpe_test (000,001,002,003).
 * ARQ_TEST_DB_URL/USER/PASSWORD. Nunca hace llamadas al proveedor real. */
public class CajaEmisionTest {
    private ErpDAO dao;
    private Usuario actor;
    private final List<Integer> citas=new ArrayList<>();
    @Before public void preparar() throws Exception {
        String url=System.getenv("ARQ_TEST_DB_URL");
        Assume.assumeTrue(url!=null && url.contains("/arquident_cpe_test?"));
        dao=new ErpDAO(() -> DriverManager.getConnection(url,System.getenv("ARQ_TEST_DB_USER"),System.getenv("ARQ_TEST_DB_PASSWORD")));
        actor=new Usuario(); actor.setIdUsuario(1); actor.setNombreRol("administrador");
    }
    @After public void limpiar() throws Exception {
        if(dao==null) return;
        dao.transaction(c -> {
            for(int id:citas) {
                update(c,"DELETE e FROM emisiones_cpe e JOIN pagos p ON p.id_pago=e.id_pago WHERE p.id_cita=?",id);
                update(c,"DELETE FROM pagos WHERE id_cita=?",id);
                update(c,"DELETE FROM citas WHERE id_cita=?",id);
            }
            update(c,"UPDATE usuarios SET dni='40000005' WHERE id_usuario=5");
            update(c,"DELETE FROM series_cpe_demo WHERE serie='BTST'");
            return null;
        });
    }
    private int reserva() throws SQLException {
        int id=dao.transaction(c -> {
            int n=insert(c,"INSERT INTO citas(id_paciente,id_odontologo,id_servicio,fecha,hora,estado) VALUES(5,3,1,?,?,'sin_pagar')",
                    LocalDate.now(Validar.LIMA).plusDays(1),LocalTime.of(8,0).plusMinutes(citas.size()*30L));
            insert(c,"INSERT INTO pagos(id_cita,monto,estado_pago) VALUES(?,50.00,'pendiente')",n);
            return n;
        });
        citas.add(id); return id;
    }
    private Map<String,Object> estado(int cita) throws SQLException {
        return dao.read(c -> one(c,"SELECT a.estado AS cita,p.estado_pago,e.estado AS emision,e.numero,e.http_status FROM citas a JOIN pagos p ON p.id_cita=a.id_cita LEFT JOIN emisiones_cpe e ON e.id_pago=p.id_pago WHERE a.id_cita=?",cita));
    }
    private FacturacionDirectaClient.Reply aceptado() {
        return new FacturacionDirectaClient.Reply(200,"{\"data\":{\"error\":false,\"respuesta_sunat_codigo\":\"0\",\"respuesta_sunat_descripcion\":\"Aceptado de prueba unitaria\"}}");
    }
    @Test public void timeoutConservaPagoYNoPermiteReenvioAlCobrarOtraVez() throws Exception {
        int id=reserva(); AtomicInteger n=new AtomicInteger();
        FacturacionDirectaClient client=new FacturacionDirectaClient(true,"BTST",request -> {
            n.incrementAndGet();
            try { assertEquals("pagado",estado(id).get("estado_pago")); } // Otra conexión ve el commit ANTES de HTTP.
            catch(SQLException e) { throw new AssertionError(e); }
            throw new HttpTimeoutException("test");
        });
        CajaService caja=new CajaService(dao,client);
        assertTrue(caja.cobrar(actor,id,"yape").contains("NO_CONFIRMADO"));
        Map<String,Object> e=estado(id);
        assertEquals("pagado",e.get("estado_pago")); assertEquals("confirmada",e.get("cita"));
        assertEquals("NO_CONFIRMADO",e.get("emision"));
        try { caja.cobrar(actor,id,"yape"); fail("No debe repetir un cobro"); }
        catch(ReglaNegocio ex) { assertEquals(409,ex.getStatus()); }
        assertEquals(1,n.get());
    }
    @Test public void dosCobrosConcurrentesGeneranUnSoloEnvio() throws Exception {
        int id=reserva(); AtomicInteger n=new AtomicInteger();
        CajaService caja=new CajaService(dao,new FacturacionDirectaClient(true,"BTST",request -> { n.incrementAndGet(); return aceptado(); }));
        ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch listo=new CountDownLatch(2);
        Callable<Integer> cobrar=() -> {
            listo.countDown(); assertTrue(listo.await(5,TimeUnit.SECONDS));
            try { caja.cobrar(actor,id,"efectivo"); return 200; }
            catch(ReglaNegocio e) { return e.getStatus(); }
        };
        try {
            Future<Integer> a=pool.submit(cobrar),b=pool.submit(cobrar);
            List<Integer> resultados=Arrays.asList(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS));
            Collections.sort(resultados); assertEquals(Arrays.asList(200,409),resultados);
        } finally { pool.shutdownNow(); }
        assertEquals(1,n.get()); assertEquals("ACEPTADO_DEMO",estado(id).get("emision"));
    }
    @Test public void datosInvalidosReviertenPagoCitaYReservaDeNumero() throws Exception {
        int id=reserva();
        dao.transaction(c -> { update(c,"UPDATE usuarios SET dni=NULL WHERE id_usuario=5"); return null; });
        CajaService caja=new CajaService(dao,new FacturacionDirectaClient(true,"BTST",request -> { throw new AssertionError("No debe llamar HTTP"); }));
        try { caja.cobrar(actor,id,"efectivo"); fail("DNI obligatorio"); } catch(ReglaNegocio esperado) { }
        Map<String,Object> e=estado(id);
        assertEquals("pendiente",e.get("estado_pago")); assertEquals("sin_pagar",e.get("cita")); assertNull(e.get("emision"));
        assertNull(dao.read(c -> one(c,"SELECT * FROM series_cpe_demo WHERE serie='BTST'")));
    }
    @Test public void correlativoPersisteAlCrearOtroServicio() throws Exception {
        int a=reserva(),b=reserva();
        new CajaService(dao,new FacturacionDirectaClient(true,"BTST",r -> aceptado())).cobrar(actor,a,"tarjeta");
        new CajaService(dao,new FacturacionDirectaClient(true,"BTST",r -> aceptado())).cobrar(actor,b,"plin");
        assertEquals(number(estado(a),"numero")+1,number(estado(b),"numero"));
    }
    @Test public void falloAlGuardarRespuestaNoOcultaQueElCobroFueConfirmado() throws Exception {
        int id=reserva(); AtomicBoolean desconectado=new AtomicBoolean();
        ErpDAO interrumpido=new ErpDAO(() -> {
            if(desconectado.get()) throw new IllegalStateException("Conexión interrumpida de prueba");
            return DriverManager.getConnection(System.getenv("ARQ_TEST_DB_URL"),System.getenv("ARQ_TEST_DB_USER"),System.getenv("ARQ_TEST_DB_PASSWORD"));
        });
        FacturacionDirectaClient client=new FacturacionDirectaClient(true,"BTST",r -> { desconectado.set(true); return aceptado(); });
        String mensaje=new CajaService(interrumpido,client).cobrar(actor,id,"yape");
        assertTrue(mensaje.contains("El cobro está registrado"));
        assertTrue(mensaje.contains("No se pudo guardar la respuesta"));
        assertEquals("pagado",estado(id).get("estado_pago"));
        assertEquals("ENVIANDO",estado(id).get("emision"));
    }
}
