package padron.dto;

public class ErrorRespuesta {

    private final String codigo;
    private final String mensaje;

    public ErrorRespuesta(String codigo, String mensaje) {
        this.codigo  = codigo;
        this.mensaje = mensaje;
    }

    public String getCodigo()  { return codigo; }
    public String getMensaje() { return mensaje; }
}