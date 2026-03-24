package padron.util;

import padron.dto.DireccionDTO;
import padron.dto.ErrorRespuesta;
import padron.dto.PersonaDTO;
import padron.dto.RespuestaPadron;

public final class Serializador {

    private Serializador() {}

    // =========================
    // JSON
    // =========================
    public static String toJson(RespuestaPadron r) {
        if (r == null) {
            return "{\"ok\":false,\"error\":{\"codigo\":\"NULL\",\"mensaje\":\"Respuesta nula\"}}";
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append("{");
        sb.append("\"ok\":").append(r.isOk());

        if (r.isOk()) {
            sb.append(",\"persona\":").append(personaJson(r.getPersona()));
            sb.append(",\"direccion\":").append(direccionJson(r.getDireccion()));
        } else {
            sb.append(",\"error\":").append(errorJson(r.getError()));
        }

        sb.append("}");
        return sb.toString();
    }

    private static String personaJson(PersonaDTO p) {
        if (p == null) return "null";
        return "{"
                + "\"cedula\":" + Escapes.quoteJson(p.getCedula())
                + ",\"nombre\":" + Escapes.quoteJson(p.getNombre())
                + ",\"primerApellido\":" + Escapes.quoteJson(p.getPrimerApellido())
                + ",\"segundoApellido\":" + Escapes.quoteJson(p.getSegundoApellido())
                + ",\"codElec\":" + Escapes.quoteJson(p.getCodElec())
                + "}";
    }

    private static String direccionJson(DireccionDTO d) {
        if (d == null) return "null";
        return "{"
                + "\"provincia\":" + Escapes.quoteJson(d.getProvincia())
                + ",\"canton\":" + Escapes.quoteJson(d.getCanton())
                + ",\"distrito\":" + Escapes.quoteJson(d.getDistrito())
                + ",\"recinto\":" + Escapes.quoteJson(d.getRecinto())
                + "}";
    }

    private static String errorJson(ErrorRespuesta e) {
        if (e == null) {
            return "{\"codigo\":\"UNKNOWN\",\"mensaje\":\"Error desconocido\"}";
        }
        return "{"
                + "\"codigo\":" + Escapes.quoteJson(e.getCodigo())
                + ",\"mensaje\":" + Escapes.quoteJson(e.getMensaje())
                + "}";
    }

    // =========================
    // XML
    // =========================
    public static String toXml(RespuestaPadron r) {
        if (r == null) {
            return "<respuesta ok=\"false\"><error><codigo>NULL</codigo><mensaje>Respuesta nula</mensaje></error></respuesta>";
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append("<respuesta ok=\"").append(r.isOk()).append("\">");

        if (r.isOk()) {
            sb.append(personaXml(r.getPersona()));
            sb.append(direccionXml(r.getDireccion()));
        } else {
            sb.append(errorXml(r.getError()));
        }

        sb.append("</respuesta>");
        return sb.toString();
    }

    private static String personaXml(PersonaDTO p) {
        if (p == null) return "<persona/>";
        return "<persona>"
                + "<cedula>" + Escapes.xml(p.getCedula()) + "</cedula>"
                + "<nombre>" + Escapes.xml(p.getNombre()) + "</nombre>"
                + "<primerApellido>" + Escapes.xml(p.getPrimerApellido()) + "</primerApellido>"
                + "<segundoApellido>" + Escapes.xml(p.getSegundoApellido()) + "</segundoApellido>"
                + "<codElec>" + Escapes.xml(p.getCodElec()) + "</codElec>"
                + "</persona>";
    }

    private static String direccionXml(DireccionDTO d) {
        if (d == null) return "<direccion/>";
        return "<direccion>"
                + "<provincia>" + Escapes.xml(d.getProvincia()) + "</provincia>"
                + "<canton>" + Escapes.xml(d.getCanton()) + "</canton>"
                + "<distrito>" + Escapes.xml(d.getDistrito()) + "</distrito>"
                + "<recinto>" + Escapes.xml(d.getRecinto()) + "</recinto>"
                + "</direccion>";
    }

    private static String errorXml(ErrorRespuesta e) {
        if (e == null) {
            return "<error><codigo>UNKNOWN</codigo><mensaje>Error desconocido</mensaje></error>";
        }
        return "<error>"
                + "<codigo>" + Escapes.xml(e.getCodigo()) + "</codigo>"
                + "<mensaje>" + Escapes.xml(e.getMensaje()) + "</mensaje>"
                + "</error>";
    }
}