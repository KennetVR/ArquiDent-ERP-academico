package pe.edu.utp.sunat;

import java.math.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import pe.edu.utp.service.*;

/** Boleta académica: RUC de ejemplo, modo 0 (beta), DNI y soles. */
public final class EmisionDemo {
    public static final String RUC="20604051984";
    public final String serie,nombre,dni,direccion,servicio;
    public final int numero,cita,idServicio;
    public final LocalDateTime fecha;
    public final BigDecimal monto,gravada,igv,valorUnitario;

    public EmisionDemo(String serie,int numero,LocalDateTime fecha,int cita,int idServicio,
                       String nombre,String dni,String direccion,String servicio,BigDecimal monto) {
        if (serie==null || !serie.matches("B[A-Z0-9]{3}")) throw new ReglaNegocio("Configure una serie de boleta demo de cuatro caracteres que empiece por B.");
        if (numero<1 || numero>99999999) throw new ReglaNegocio("Correlativo demo agotado o inválido.");
        if (dni==null || !dni.matches("[0-9]{8}")) throw new ReglaNegocio("Complete el DNI de ocho dígitos del paciente antes de cobrar y emitir.");
        this.serie=serie; this.numero=numero; this.fecha=Objects.requireNonNull(fecha);
        this.cita=cita; this.idServicio=idServicio; this.dni=dni;
        this.nombre=Validar.texto(nombre,"Nombre del paciente",2,100);
        this.direccion=Validar.texto(direccion,"Dirección",0,500);
        this.servicio=Validar.texto(servicio,"Servicio",1,100);
        this.monto=Validar.dinero(monto==null?null:monto.toPlainString());
        // El precio de Caja YA incluye IGV en esta demostración. No sumar 18% al cobro.
        valorUnitario=this.monto.divide(new BigDecimal("1.18"),6,RoundingMode.HALF_UP);
        gravada=valorUnitario.setScale(2,RoundingMode.HALF_UP);
        igv=this.monto.subtract(gravada);
    }
    public String documento() { return serie+"-"+numero; }
    public String archivo() { return RUC+"-03-"+documento(); }

    public Map<String,Object> payload() {
        Map<String,Object> empresa=new LinkedHashMap<>();
        empresa.put("ruc",RUC);
        empresa.put("razon_social","FACTURACION ELECTRONICA MONSTRUO E.I.R.L.");
        empresa.put("nombre_comercial","FACTURACION INTEGRAL");
        empresa.put("domicilio_fiscal","AV. LA MOLINA NRO. 570");
        empresa.put("ubigeo","150114"); empresa.put("urbanizacion","RESIDENCIAL MONTERRICO");
        empresa.put("distrito","LA MOLINA"); empresa.put("provincia","LIMA"); empresa.put("departamento","LIMA");
        empresa.put("modo","0");
        empresa.put("usu_secundario_produccion_user","MODDATOS");
        empresa.put("usu_secundario_produccion_password","MODDATOS");
        Map<String,Object> cliente=Map.of("razon_social_nombres",nombre,"numero_documento",dni,
                "codigo_tipo_entidad","1","cliente_direccion",direccion.isBlank()?"-":direccion);
        Map<String,Object> venta=new LinkedHashMap<>();
        venta.put("serie",serie); venta.put("numero",String.valueOf(numero));
        venta.put("fecha_emision",fecha.toLocalDate().toString());
        venta.put("hora_emision",fecha.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        venta.put("fecha_vencimiento",""); venta.put("moneda_id","1"); // 1=PEN; 2=USD en este proveedor.
        venta.put("forma_pago_id","1"); // Contado, independientemente de efectivo/tarjeta/yape.
        venta.put("total_gravada",gravada.toPlainString()); venta.put("total_igv",igv.toPlainString());
        venta.put("total_exonerada","0.00"); venta.put("total_inafecta","0.00");
        venta.put("tipo_documento_codigo","03");
        venta.put("nota","PRUEBA ACADEMICA ARQUIDENT - Cita #"+cita+" - SUNAT BETA");
        Map<String,Object> item=Map.of("producto",servicio,"cantidad","1","precio_base",valorUnitario.toPlainString(),
                "codigo_sunat","85122001","codigo_producto","SERV-"+idServicio,"codigo_unidad","ZZ","tipo_igv_codigo","10");
        return Map.of("empresa",empresa,"cliente",cliente,"venta",venta,"items",List.of(item));
    }
}
