package pe.edu.utp.sunat;

/** Resultado del proveedor; HTTP 200 por sí solo no significa aceptación. */
public final class ResultadoEmision {
    public final String estado,codigo,mensaje,pdf,xml,cdr,hash;
    public final Integer httpStatus;
    public ResultadoEmision(String estado,Integer httpStatus,String codigo,String mensaje,
                            String pdf,String xml,String cdr,String hash) {
        this.estado=estado; this.httpStatus=httpStatus; this.codigo=codigo; this.mensaje=mensaje;
        this.pdf=pdf; this.xml=xml; this.cdr=cdr; this.hash=hash;
    }
    public static ResultadoEmision error(String estado,Integer status,String mensaje) {
        return new ResultadoEmision(estado,status,null,mensaje,null,null,null,null);
    }
}
