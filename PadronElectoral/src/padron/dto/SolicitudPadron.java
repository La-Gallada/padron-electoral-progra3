package padron.dto;

public class SolicitudPadron {

    private final String cedula;
    private final String formato; // "JSON" o "TEXTO"

    public SolicitudPadron(String cedula, String formato) {
        this.cedula  = cedula;
        this.formato = formato;
    }

    public String getCedula()  { return cedula; }
    public String getFormato() { return formato; }
}