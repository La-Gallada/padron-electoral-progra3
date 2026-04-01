package padron.presentacion.tcp;

import padron.dto.FormatoSalida;

public class TcpRequestParser {

    private static final String SEPARADOR = "\\|";
    private static final int PARTES_ESPERADAS = 3;

    private TcpRequestParser() {
    }

    public static TcpParseResult parsear(String linea) {
        if (linea == null) {
            return TcpParseResult.error("Línea nula recibida");
        }

        String limpia = linea.trim();
        if (limpia.isEmpty()) {
            return TcpParseResult.error("Línea vacía recibida");
        }

        if (limpia.equalsIgnoreCase("BYE")) {
            return TcpParseResult.bye();
        }

        String[] partes = limpia.split(SEPARADOR, -1);
        if (partes.length != PARTES_ESPERADAS) {
            return TcpParseResult.error(
                    "Formato inválido. Se esperaba GET|cedula|FORMATO, se recibió: " + limpia
            );
        }

        String comando = partes[0].trim().toUpperCase();
        if (!"GET".equals(comando)) {
            return TcpParseResult.error(
                    "Comando desconocido: '" + partes[0].trim() + "'. Solo se acepta GET o BYE"
            );
        }

        String cedula = partes[1].trim();
        if (cedula.isEmpty()) {
            return TcpParseResult.error("La cédula no puede estar vacía");
        }

        String formatoStr = partes[2].trim().toUpperCase();
        FormatoSalida formato;
        switch (formatoStr) {
            case "JSON":
                formato = FormatoSalida.JSON;
                break;
            case "XML":
                formato = FormatoSalida.XML;
                break;
            default:
                return TcpParseResult.error(
                        "Formato desconocido: '" + partes[2].trim() + "'. Use JSON o XML"
                );
        }

        return TcpParseResult.ok(cedula, formato);
    }
}