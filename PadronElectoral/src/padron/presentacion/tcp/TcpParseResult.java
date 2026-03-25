/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package padron.presentacion.tcp;

import padron.dto.FormatoSalida;

/**
 * el resultado inmutable del parseo de una linea tcp.
 *
 * una linea válida tiene la forma:  get|cedula|json  o  get|cedula|xml
 * una linea de cierre es:  BYE
 *
 * si el parseo falla, ok == false y errorMensaje describe el problema.
 */
public class TcpParseResult {

    private final boolean      ok;
    private final boolean      bye;
    private final String       cedula;
    private final FormatoSalida formato;
    private final String       errorMensaje;

    // constructores 

    private TcpParseResult(boolean ok, boolean bye,
                            String cedula, FormatoSalida formato,
                            String errorMensaje) {
        this.ok           = ok;
        this.bye          = bye;
        this.cedula       = cedula;
        this.formato      = formato;
        this.errorMensaje = errorMensaje;
    }

    // fabricas 

    // parseo bueno de get|cedula|fromato
    public static TcpParseResult ok(String cedula, FormatoSalida formato) {
        return new TcpParseResult(true, false, cedula, formato, null);
    }

    // el cliente envio bye para cerrar la conexion 
    public static TcpParseResult bye() {
        return new TcpParseResult(false, true, null, null, null);
    }

    // la linea no pudo interpretarse bien
    public static TcpParseResult error(String mensaje) {
        return new TcpParseResult(false, false, null, null, mensaje);
    }

    //  getters 

    // true si la solicitud es valida y lista para procesar 
    public boolean isOk()           { return ok; }

    // true si el cliente pidio cerrar la conexion  
    public boolean isBye()          { return bye; }

    // cedula extraida de la linea (solo cuando isok() == true) 
    public String getCedula()       { return cedula; }

    // formato de salida (solo cuando isok()==true)
    public FormatoSalida getFormato() { return formato; }

    // descripcion del error (solo cuando isok() == false && isbye() == false)
    public String getErrorMensaje() { return errorMensaje; }
}