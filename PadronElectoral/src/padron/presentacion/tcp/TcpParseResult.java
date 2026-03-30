package padron.presentacion.tcp;

import padron.dto.FormatoSalida;

public class TcpParseResult {

    private final boolean ok;
    private final boolean bye;
    private final String cedula;
    private final FormatoSalida formato;
    private final String errorMensaje;

    private TcpParseResult(boolean ok, boolean bye, String cedula, FormatoSalida formato, String errorMensaje) {
        this.ok = ok;
        this.bye = bye;
        this.cedula = cedula;
        this.formato = formato;
        this.errorMensaje = errorMensaje;
    }

    public static TcpParseResult ok(String cedula, FormatoSalida formato) {
        return new TcpParseResult(true, false, cedula, formato, null);
    }

    public static TcpParseResult bye() {
        return new TcpParseResult(false, true, null, null, null);
    }

    public static TcpParseResult error(String mensaje) {
        return new TcpParseResult(false, false, null, null, mensaje);
    }

    public boolean isOk() {
        return ok;
    }

    public boolean isBye() {
        return bye;
    }

    public String getCedula() {
        return cedula;
    }

    public FormatoSalida getFormato() {
        return formato;
    }

    public String getErrorMensaje() {
        return errorMensaje;
    }
}