/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package padron.presentacion.tcp;

import padron.dto.FormatoSalida;

/**
 * parsea una linea del protocolo tcp del padron.
 *
 * protocolo esperado (una linea por solicitud, terminada en \n):
 *   get|cedula|json
 *   get|cedula|xml
 *   bye
 *
 * reglas:
 *  - el separador es "|"
 *  - el comando debe ser "get" 
 *  - la cédula no puede estar vacía
 *  - el formato debe ser "json" o "xml" 
 */
public class TcpRequestParser {

    private static final String SEPARADOR = "\\|";
    private static final int    PARTES_ESPERADAS = 3;

    private TcpRequestParser() {}   

    /**
     * parsea una linea recibida del cliente tcp.
     *
     * @param linea  texto crudo recibido (puede tener \r\n al final)
     * @return       TcpParseResult que describe el resultado
     */
    public static TcpParseResult parsear(String linea) {

        // limpiar espacios y saltos de linea
        if (linea == null) {
            return TcpParseResult.error("Línea nula recibida");
        }
        String limpia = linea.trim();

        if (limpia.isEmpty()) {
            return TcpParseResult.error("Línea vacía recibida");
        }

        // detectar bye
        if (limpia.equalsIgnoreCase("BYE")) {
            return TcpParseResult.bye();
        }

        // separar por "|"
        String[] partes = limpia.split(SEPARADOR, -1);
        if (partes.length != PARTES_ESPERADAS) {
            return TcpParseResult.error(
                "Formato inválido. Se esperaba GET|cedula|FORMATO, se recibió: " + limpia
            );
        }

        // validar comando
        String comando = partes[0].trim().toUpperCase();
        if (!"GET".equals(comando)) {
            return TcpParseResult.error(
                "Comando desconocido: '" + partes[0].trim() + "'. Solo se acepta GET o BYE"
            );
        }

        // validar cedula (no vacia; la normalizacion la hace otra clase)
        String cedula = partes[1].trim();
        if (cedula.isEmpty()) {
            return TcpParseResult.error("La cédula no puede estar vacía");
        }

        // validar el formato
        String formatoStr = partes[2].trim().toUpperCase();
        FormatoSalida formato;
        switch (formatoStr) {
            case "JSON": formato = FormatoSalida.JSON; break;
            case "XML":  formato = FormatoSalida.XML;  break;
            default:
                return TcpParseResult.error(
                    "Formato desconocido: '" + partes[2].trim() + "'. Use JSON o XML"
                );
        }

        return TcpParseResult.ok(cedula, formato);
    }
}