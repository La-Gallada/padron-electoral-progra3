package padron.util;

public final class Escapes {

    private Escapes() {
        // utilidad: no instanciable
    }

    /**
     
     */
    public static String json(String s) {
        if (s == null) return "";

        StringBuilder out = new StringBuilder(s.length() + 16);

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"':  out.append("\\\""); break;
                case '\n': out.append("\\n");  break;
                case '\r': out.append("\\r");  break;
                case '\t': out.append("\\t");  break;
                case '\b': out.append("\\b");  break;
                case '\f': out.append("\\f");  break;
                default:
                    // control chars (0x00 - 0x1F) en JSON deben escaparse
                    if (c <= 0x1F) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }

        return out.toString();
    }

    /**
     * Escapa texto para que sea seguro dentro de XML.
     */
    public static String xml(String s) {
        if (s == null) return "";

        StringBuilder out = new StringBuilder(s.length() + 16);

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            switch (c) {
                case '&':  out.append("&amp;");  break;
                case '<':  out.append("&lt;");   break;
                case '>':  out.append("&gt;");   break;
                case '"':  out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default:
                    out.append(c);
            }
        }

        return out.toString();
    }

    /**
     * Útil para JSON cuando tengas que poner un valor texto como string.
     * Devuelve con comillas y escapado.
     * Ej: quoteJson("hola") -> "hola"
     */
    public static String quoteJson(String s) {
        return "\"" + json(s) + "\"";
    }
}