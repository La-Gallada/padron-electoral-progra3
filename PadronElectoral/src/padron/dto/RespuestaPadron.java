package padron.dto;

public class RespuestaPadron {

    private final boolean exito;
    private final PersonaDTO persona;
    private final DireccionDTO direccion;
    private final String error;

    // Constructor para respuesta exitosa
    public RespuestaPadron(PersonaDTO persona, DireccionDTO direccion) {
        this.exito     = true;
        this.persona   = persona;
        this.direccion = direccion;
        this.error     = null;
    }

    // Constructor para respuesta de error
    public RespuestaPadron(String error) {
        this.exito     = false;
        this.persona   = null;
        this.direccion = null;
        this.error     = error;
    }

    public boolean isExito()        { return exito; }
    public PersonaDTO getPersona()  { return persona; }
    public DireccionDTO getDireccion() { return direccion; }
    public String getError()        { return error; }
}