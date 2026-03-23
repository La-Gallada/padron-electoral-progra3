package padron.dto;

public class RespuestaPadron {
    private final boolean ok;
    private final PersonaDTO persona;      // null si error
    private final DireccionDTO direccion;  // null si error
    private final ErrorRespuesta error;    // null si ok

    private RespuestaPadron(boolean ok, PersonaDTO persona, DireccionDTO direccion, ErrorRespuesta error) {
        this.ok = ok;
        this.persona = persona;
        this.direccion = direccion;
        this.error = error;
    }

    public static RespuestaPadron ok(PersonaDTO persona, DireccionDTO direccion) {
        return new RespuestaPadron(true, persona, direccion, null);
    }

    public static RespuestaPadron error(String codigo, String mensaje) {
        return new RespuestaPadron(false, null, null, new ErrorRespuesta(codigo, mensaje));
    }

    public boolean isOk() { return ok; }
    public PersonaDTO getPersona() { return persona; }
    public DireccionDTO getDireccion() { return direccion; }
    public ErrorRespuesta getError() { return error; }
}