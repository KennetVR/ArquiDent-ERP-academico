package pe.edu.utp.sunat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import pe.edu.utp.service.*;

/** Datos de un documento YA emitido fuera de ArquiDent. Alcance: facturas y boletas. */
public final class Comprobante {
    public final String ruc, tipo, serie;
    public final int numero;
    public final LocalDate fecha;
    public final BigDecimal monto;
    public Comprobante(String ruc,String tipo,String serie,int numero,LocalDate fecha,BigDecimal monto) {
        if (ruc==null || !ruc.matches("[0-9]{11}")) throw new ReglaNegocio("RUC del emisor: 11 dígitos.");
        if (!Arrays.asList("01","03").contains(tipo)) throw new ReglaNegocio("Seleccione factura (01) o boleta (03).");
        if (serie==null || !serie.matches("[A-Z0-9]{4}")) throw new ReglaNegocio("Serie: cuatro caracteres alfanuméricos en mayúscula.");
        if (numero<1 || numero>99999999) throw new ReglaNegocio("Número: entre 1 y 99999999.");
        if (fecha==null || fecha.isAfter(LocalDate.now(Validar.LIMA)) || fecha.isBefore(LocalDate.of(1900,1,1))) throw new ReglaNegocio("Fecha de emisión inválida.");
        this.ruc=ruc; this.tipo=tipo; this.serie=serie; this.numero=numero; this.fecha=fecha;
        this.monto=Validar.dinero(monto==null?null:monto.toPlainString());
    }
    public Map<String,Object> payload() {
        Map<String,Object> p=new LinkedHashMap<>();
        p.put("numRuc",ruc); p.put("codComp",tipo); p.put("numeroSerie",serie); p.put("numero",numero);
        p.put("fechaEmision",fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))); p.put("monto",monto);
        return p;
    }
}
