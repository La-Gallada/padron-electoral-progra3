package padron.presentacion.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import padron.dto.FormatoSalida;
import padron.dto.PadronPageResponse;
import padron.dto.RespuestaPadron;
import padron.dto.SolicitudPadron;
import padron.logica.ServicioPadron;
import padron.util.Serializador;

public class PadronHttpHandler implements HttpHandler {

    private final ServicioPadron servicio;

    public PadronHttpHandler(ServicioPadron servicio) {
        this.servicio = servicio;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String cuerpo = serializarError(
                    "METODO_NO_PERMITIDO",
                    "Método no permitido. Use GET.",
                    FormatoSalida.JSON
            );
            enviarRespuesta(exchange, 405, contentType(FormatoSalida.JSON), cuerpo);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path == null) {
            path = "";
        }

        path = path.trim();

        if ("/padron/explorar".equalsIgnoreCase(path) || path.endsWith("/padron/explorar")) {
            manejarExplorar(exchange);
            return;
        }

        manejarConsultaIndividual(exchange, path);
    }

    private void manejarConsultaIndividual(HttpExchange exchange, String path) throws IOException {
        Map<String, String> params = parsearQueryString(exchange.getRequestURI().getRawQuery());

        String cedula = params.get("cedula");
        if (cedula == null || cedula.trim().isEmpty()) {
            cedula = extraerCedulaDesdePath(path);
        }

        if (cedula == null || cedula.trim().isEmpty()) {
            String cuerpo = serializarError(
                    "CEDULA_VACIA",
                    "El parámetro 'cedula' es obligatorio.",
                    FormatoSalida.JSON
            );
            enviarRespuesta(exchange, 400, contentType(FormatoSalida.JSON), cuerpo);
            return;
        }

        String formatoRaw = params.getOrDefault("format", params.getOrDefault("formato", "JSON"));
        FormatoSalida formato = parsearFormato(formatoRaw);

        if (formato == null) {
            String cuerpo = serializarError(
                    "FORMATO_INVALIDO",
                    "Formato desconocido: '" + formatoRaw + "'. Use JSON o XML.",
                    FormatoSalida.JSON
            );
            enviarRespuesta(exchange, 400, contentType(FormatoSalida.JSON), cuerpo);
            return;
        }

        SolicitudPadron solicitud = new SolicitudPadron(cedula.trim(), formato);
        RespuestaPadron respuesta;

        try {
            respuesta = servicio.atender(solicitud);
        } catch (Exception e) {
            System.err.println("[HTTP] Error en ServicioPadron: " + e.getMessage());
            String cuerpo = serializarError(
                    "ERROR_INTERNO",
                    "Error interno del servidor.",
                    formato
            );
            enviarRespuesta(exchange, 500, contentType(formato), cuerpo);
            return;
        }

        int status = respuesta.isOk() ? 200 : mapearStatusConsulta(respuesta);
        String cuerpo = serializar(respuesta, formato);
        enviarRespuesta(exchange, status, contentType(formato), cuerpo);
    }

    private void manejarExplorar(HttpExchange exchange) throws IOException {
        Map<String, String> params = parsearQueryString(exchange.getRequestURI().getRawQuery());

        String criterio = params.getOrDefault("criterio", "").trim();
        int pagina = parsearEnteroPositivo(params.get("pagina"), 1);
        int tamano = parsearEnteroPositivo(params.get("tamano"), 100);
        String ordenarPor = params.getOrDefault("ordenarPor", "cedula").trim();
        String direccion = params.getOrDefault("direccion", "asc").trim();

        String formatoRaw = params.getOrDefault("format", params.getOrDefault("formato", "JSON"));
        FormatoSalida formato = parsearFormato(formatoRaw);

        if (formato == null) {
            String cuerpo = serializarError(
                    "FORMATO_INVALIDO",
                    "Formato desconocido: '" + formatoRaw + "'. Use JSON o XML.",
                    FormatoSalida.JSON
            );
            enviarRespuesta(exchange, 400, contentType(FormatoSalida.JSON), cuerpo);
            return;
        }

        PadronPageResponse respuesta = servicio.explorar(criterio, pagina, tamano, ordenarPor, direccion);
        int status = respuesta.isOk() ? 200 : mapearStatusPagina(respuesta);
        String cuerpo = serializarPagina(respuesta, formato);

        enviarRespuesta(exchange, status, contentType(formato), cuerpo);
    }

    private int mapearStatusConsulta(RespuestaPadron respuesta) {
        if (respuesta == null || respuesta.getError() == null) {
            return 500;
        }

        String codigo = respuesta.getError().getCodigo();
        if (codigo == null) {
            return 500;
        }

        switch (codigo) {
            case "CEDULA_VACIA":
            case "CEDULA_INVALIDA":
            case "SOLICITUD_INVALIDA":
            case "FORMATO_INVALIDO":
                return 400;
            case "CEDULA_NO_ENCONTRADA":
            case "NO_ENCONTRADA":
                return 404;
            default:
                return 500;
        }
    }

    private int mapearStatusPagina(PadronPageResponse respuesta) {
        if (respuesta == null || respuesta.getError() == null) {
            return 500;
        }

        String codigo = respuesta.getError().getCodigo();
        if (codigo == null) {
            return 500;
        }

        switch (codigo) {
            case "PAGINA_INVALIDA":
            case "TAMANO_INVALIDO":
                return 400;
            default:
                return 500;
        }
    }

    private String extraerCedulaDesdePath(String path) {
        if (path == null) {
            return null;
        }

        if (!path.startsWith("/padron/")) {
            return null;
        }

        String resto = path.substring("/padron/".length()).trim();

        if (resto.isEmpty() || resto.contains("/") || "explorar".equalsIgnoreCase(resto)) {
            return null;
        }

        return resto;
    }

    private Map<String, String> parsearQueryString(String queryString) {
        Map<String, String> mapa = new HashMap<>();

        if (queryString == null || queryString.isEmpty()) {
            return mapa;
        }

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

    private String decodificar(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return s;
        }
    }

    private int parsearEnteroPositivo(String raw, int valorPorDefecto) {
        if (raw == null || raw.trim().isEmpty()) {
            return valorPorDefecto;
        }

        try {
            int valor = Integer.parseInt(raw.trim());
            return valor > 0 ? valor : valorPorDefecto;
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    private FormatoSalida parsearFormato(String raw) {
        if (raw == null) {
            return FormatoSalida.JSON;
        }

        String upper = raw.trim().toUpperCase();
        switch (upper) {
            case "JSON":
                return FormatoSalida.JSON;
            case "XML":
                return FormatoSalida.XML;
            default:
                return null;
        }
    }

    private String serializar(RespuestaPadron respuesta, FormatoSalida formato) {
        return formato == FormatoSalida.XML
                ? Serializador.toXml(respuesta)
                : Serializador.toJson(respuesta);
    }

    private String serializarPagina(PadronPageResponse respuesta, FormatoSalida formato) {
        return formato == FormatoSalida.XML
                ? Serializador.toXml(respuesta)
                : Serializador.toJson(respuesta);
    }

    private String serializarError(String codigo, String mensaje, FormatoSalida formato) {
        return serializar(RespuestaPadron.error(codigo, mensaje), formato);
    }

    private String contentType(FormatoSalida formato) {
        return formato == FormatoSalida.XML
                ? "application/xml; charset=UTF-8"
                : "application/json; charset=UTF-8";
    }

    private void enviarRespuesta(HttpExchange exchange, int codigo, String contentType, String cuerpo)
            throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(codigo, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }

        exchange.close();
    }
}