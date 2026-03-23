package padron.dto;

public class PersonaDTO {

    private final String cedula;
    private final String nombre;
    private final String primerApellido;
    private final String segundoApellido;
    private final String codElec;

    public PersonaDTO(String cedula, String nombre, String primerApellido,
                      String segundoApellido, String codElec) {
        this.cedula          = cedula;
        this.nombre          = nombre;
        this.primerApellido  = primerApellido;
        this.segundoApellido = segundoApellido;
        this.codElec         = codElec;
    }

    public String getCedula()          { return cedula; }
    public String getNombre()          { return nombre; }
    public String getPrimerApellido()  { return primerApellido; }
    public String getSegundoApellido() { return segundoApellido; }
    public String getCodElec()         { return codElec; }
}