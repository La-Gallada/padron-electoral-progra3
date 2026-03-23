package padron.dto;

public class PersonaDTO {
    private final String cedula;
    private final String nombreCompleto;

    public PersonaDTO(String cedula, String nombreCompleto) {
        this.cedula = cedula;
        this.nombreCompleto = nombreCompleto;
    }

    public String getCedula() { return cedula; }
    public String getNombreCompleto() { return nombreCompleto; }
}