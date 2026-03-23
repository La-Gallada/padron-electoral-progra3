package padron.dto;

public class SolicitudPadron {

    private final String       cedula;
    private final FormatoSalida formato;

    public SolicitudPadron(String cedula, FormatoSalida formato) {
        this.cedula  = cedula;
        this.formato = formato;
    }

    public String        getCedula()  { return cedula; }
    public FormatoSalida getFormato() { return formato; }
}