package padron.presentacion.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import padron.dto.FormatoSalida;
import padron.dto.RespuestaPadron;
import padron.dto.SolicitudPadron;
import padron.logica.ServicioPadron;
import padron.util.Serializador;

/**
 * handler http para el endpoint /padron.
 *
 * metodo aceptado: get
 *
 * parametros de consulta:
 *   cedula  – numero de cedula costarricense
 *   formato – json (default) o xml
 *
 * respuestas:
 *   200 ok           – solicitud procesada (ok: true o error de negocio ok: false)
 *   400 bad request  – parametros faltantes o formato invalido
 *   405 method not allowed – metodo distinto de get
 *   500 internal server error – excepcion inesperada
 */
public class PadronHttpHandler implements HttpHandler {

    private final ServicioPadron servicio;

    public PadronHttpHandler(ServicioPadron servicio) {
        this.servicio = servicio;
    }

    // HttpHandler
   
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // solo aceptar get.
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarRespuesta(exchange, 405, "text/plain", "Método no permitido. Use GET.");
            return;
        }

        // parsear parametros de la url.
        String queryString = exchange.getRequestURI().getRawQuery();
        Map<String, String> params = parsearQueryString(queryString);

        // validar parametro cedula
        String cedula = params.get("cedula");
        if (cedula == null || cedula.trim().isEmpty()) {
            String cuerpo = serializarError("PARAMETRO_FALTANTE",
                "El parámetro 'cedula' es obligatorio", FormatoSalida.JSON);
            enviarRespuesta(exchange, 400, contentType(FormatoSalida.JSON), cuerpo);
            return;
        }
        cedula = cedula.trim();

        // validar parametro formato (opcional, json por defecto)
        String formatoStr = params.getOrDefault("formato", "JSON").trim().toUpperCase();
        FormatoSalida formato;
        switch (formatoStr) {
            case "JSON": formato = FormatoSalida.JSON; break;
            case "XML":  formato = FormatoSalida.XML;  break;
            default:
                String cuerpo = serializarError("FORMATO_INVALIDO",
                    "Formato desconocido: '" + formatoStr + "'. Use JSON o XML",
                    FormatoSalida.JSON);
                enviarRespuesta(exchange, 400, contentType(FormatoSalida.JSON), cuerpo);
                return;
        }

        // construir SolicitudPadron.
        SolicitudPadron solicitud = new SolicitudPadron(cedula, formato);

        //  llamar a la logica de negocio.
        RespuestaPadron respuesta;
        try {
            respuesta = servicio.atender(solicitud);
        } catch (Exception e) {
            System.err.println("[HTTP] Error en ServicioPadron: " + e.getMessage());
            String cuerpo = serializarError("ERROR_INTERNO",
                "Error interno del servidor: " + e.getMessage(), formato);
            enviarRespuesta(exchange, 500, contentType(formato), cuerpo);
            return;
        }

        //  serializar y enviar
        //  http 200 siempre: el ok/error va dentro del cuerpo (siguiendo el contrato del DTO)
        String cuerpo = serializar(respuesta, formato);
        enviarRespuesta(exchange, 200, contentType(formato), cuerpo);
    }

    // helpers privados
    
    /**
     * parsea "cedula = 123 & formato = json" a un Map.
     * devuelve un mapa vacio si queryString es null.
     */
    private Map<String, String> parsearQueryString(String queryString) {
        Map<String, String> mapa = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) return mapa;

        for (String par : queryString.split("&")) {
            int idx = par.indexOf('=');
            if (idx > 0) {
                String clave = decodificar(par.substring(0, idx));
                String valor = decodificar(par.substring(idx + 1));
                mapa.put(clave, valor);
            }
        }
        return mapa;
    }

    // decodifica (sin dependencias externas). 
    private String decodificar(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return s; // si falla, devolver tal cual
        }
    }

    //  serializa al formato indicado.
    private String serializar(RespuestaPadron respuesta, FormatoSalida formato) {
        return formato == FormatoSalida.XML
            ? Serializador.toXml(respuesta)
            : Serializador.toJson(respuesta);
    }

    //  construye y serializa un error antes de llamar al servicio. 
    private String serializarError(String codigo, String mensaje, FormatoSalida formato) {
        return serializar(RespuestaPadron.error(codigo, mensaje), formato);
    }

    //  devuelve el content type correcto para el formato. 
    private String contentType(FormatoSalida formato) {
        return formato == FormatoSalida.XML
            ? "application/xml; charset=UTF-8"
            : "application/json; charset=UTF-8";
    }

    /**
     * envia la respuesta http con codigo, content type y cuerpo.
     * siempre cierra el exchange.
     */
    private void enviarRespuesta(HttpExchange exchange, int codigo,
                                  String contentType, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(codigo, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        exchange.close();
    }
}