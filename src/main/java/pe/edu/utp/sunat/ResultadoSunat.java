package pe.edu.utp.sunat;

/** Resultado de cada intento. Un error técnico NO determina la validez del documento. */
public final class ResultadoSunat {
    public final String resultado, estadoCp, estadoRuc, condicion, codigoError, mensaje, observaciones;
    public final Integer httpStatus;
    public ResultadoSunat(String resultado,Integer httpStatus,String cp,String ruc,String condicion,String codigo,String mensaje,String observaciones) {
        this.resultado=resultado; this.httpStatus=httpStatus; this.estadoCp=cp; this.estadoRuc=ruc;
        this.condicion=condicion; this.codigoError=codigo; this.mensaje=mensaje; this.observaciones=observaciones;
    }
    public static ResultadoSunat error(String tipo,Integer status,String mensaje) {
        return new ResultadoSunat(tipo,status,null,null,null,null,mensaje,null);
    }
}
