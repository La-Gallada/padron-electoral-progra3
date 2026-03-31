package padron.presentacion.gui;

public class ConsultaSeleccionada {

    private final String cedula;
    private final String nombre;
    private final String primerApellido;
    private final String segundoApellido;
    private final String codElec;
    private final String provincia;
    private final String canton;
    private final String distrito;
    private final String recinto;
    private final String rawJson;
    private final String rawXml;

    public ConsultaSeleccionada(
            String cedula,
            String nombre,
            String primerApellido,
            String segundoApellido,
            String codElec,
            String provincia,
            String canton,
            String distrito,
            String recinto,
            String rawJson,
            String rawXml
    ) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.codElec = codElec;
        this.provincia = provincia;
        this.canton = canton;
        this.distrito = distrito;
        this.recinto = recinto;
        this.rawJson = rawJson;
        this.rawXml = rawXml;
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public String getCodElec() {
        return codElec;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getCanton() {
        return canton;
    }

    public String getDistrito() {
        return distrito;
    }

    public String getRecinto() {
        return recinto;
    }

    public String getRawJson() {
        return rawJson;
    }

    public String getRawXml() {
        return rawXml;
    }

    public String getNombreCompleto() {
        return (nombre + " " + primerApellido + " " + segundoApellido).trim();
    }
}