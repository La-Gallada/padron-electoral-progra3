package padron.presentacion.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonGuiParser {

    private JsonGuiParser() {
    }

    public static PadronPageData parsePadronPage(String json) throws IOException {
        if (json == null || json.trim().isEmpty()) {
            throw new IOException("La respuesta del servidor está vacía.");
        }

        boolean ok = extractBoolean(json, "ok", false);
        if (!ok) {
            String errorObj = extractObject(json, "error");
            String codigo = extractString(errorObj, "codigo");
            String mensaje = extractString(errorObj, "mensaje");

            return PadronPageData.error(
                    codigo == null ? "UNKNOWN" : codigo,
                    mensaje == null ? "Error desconocido" : mensaje
            );
        }

        String criterio = extractString(json, "criterio");
        int paginaActual = extractInt(json, "paginaActual", 1);
        int tamanoPagina = extractInt(json, "tamanoPagina", 100);
        int totalResultados = extractInt(json, "totalResultados", 0);
        int totalPaginas = extractInt(json, "totalPaginas", 1);
        String ordenarPor = extractString(json, "ordenarPor");
        String direccion = extractString(json, "direccion");

        String arrayResultados = extractArray(json, "resultados");
        List<PadronRow> filas = new ArrayList<>();

        if (arrayResultados != null && !arrayResultados.trim().isEmpty()) {
            for (String itemJson : splitObjects(arrayResultados)) {
                filas.add(new PadronRow(
                        safe(extractString(itemJson, "cedula")),
                        safe(extractString(itemJson, "nombre")),
                        safe(extractString(itemJson, "primerApellido")),
                        safe(extractString(itemJson, "segundoApellido")),
                        safe(extractString(itemJson, "codElec"))
                ));
            }
        }

        return PadronPageData.ok(
                criterio,
                paginaActual,
                tamanoPagina,
                totalResultados,
                totalPaginas,
                ordenarPor,
                direccion,
                filas
        );
    }

    public static ConsultaSeleccionada parseConsultaSeleccionada(String json, String rawJson, String rawXml) throws IOException {
        if (json == null || json.trim().isEmpty()) {
            throw new IOException("La respuesta del servidor está vacía.");
        }

        boolean ok = extractBoolean(json, "ok", false);
        if (!ok) {
            String errorObj = extractObject(json, "error");
            String mensaje = extractString(errorObj, "mensaje");

            if (mensaje == null || mensaje.trim().isEmpty()) {
                mensaje = "No se pudo consultar la persona seleccionada.";
            }

            throw new IOException(mensaje);
        }

        String personaObj = extractObject(json, "persona");
        if (personaObj == null) {
            throw new IOException("La respuesta no contiene datos de persona.");
        }

        String direccionObj = extractObject(json, "direccion");

        return new ConsultaSeleccionada(
                safe(extractString(personaObj, "cedula")),
                safe(extractString(personaObj, "nombre")),
                safe(extractString(personaObj, "primerApellido")),
                safe(extractString(personaObj, "segundoApellido")),
                safe(extractString(personaObj, "codElec")),
                direccionObj == null ? "" : safe(extractString(direccionObj, "provincia")),
                direccionObj == null ? "" : safe(extractString(direccionObj, "canton")),
                direccionObj == null ? "" : safe(extractString(direccionObj, "distrito")),
                direccionObj == null ? "" : safe(extractString(direccionObj, "recinto")),
                rawJson == null ? "" : rawJson,
                rawXml == null ? "" : rawXml
        );
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean extractBoolean(String json, String key, boolean defaultValue) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }

        return defaultValue;
    }

    private static int extractInt(String json, String key, int defaultValue) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return defaultValue;
    }

    private static String extractString(String json, String key) {
        if (json == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }

        return null;
    }

    private static String extractObject(String json, String key) {
        return extractStructure(json, key, '{', '}');
    }

    private static String extractArray(String json, String key) {
        return extractStructure(json, key, '[', ']');
    }

    private static String extractStructure(String json, String key, char open, char close) {
        if (json == null) {
            return null;
        }

        String token = "\"" + key + "\"";
        int keyIndex = json.indexOf(token);

        if (keyIndex < 0) {
            return null;
        }

        int colonIndex = json.indexOf(':', keyIndex);
        if (colonIndex < 0) {
            return null;
        }

        int index = colonIndex + 1;
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }

        if (index >= json.length()) {
            return null;
        }

        if (json.startsWith("null", index)) {
            return null;
        }

        if (json.charAt(index) != open) {
            return null;
        }

        int depth = 0;
        for (int i = index; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == open) {
                depth++;
            } else if (c == close) {
                depth--;
                if (depth == 0) {
                    return json.substring(index, i + 1);
                }
            }
        }

        return null;
    }

    private static List<String> splitObjects(String arrayJson) {
        List<String> objetos = new ArrayList<>();

        if (arrayJson == null || arrayJson.length() < 2) {
            return objetos;
        }

        String contenido = arrayJson.substring(1, arrayJson.length() - 1).trim();
        if (contenido.isEmpty()) {
            return objetos;
        }

        int depth = 0;
        int inicio = -1;

        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);

            if (c == '{') {
                if (depth == 0) {
                    inicio = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && inicio >= 0) {
                    objetos.add(contenido.substring(inicio, i + 1));
                    inicio = -1;
                }
            }
        }

        return objetos;
    }

    private static String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\b", "\b")
                .replace("\\f", "\f");
    }
}