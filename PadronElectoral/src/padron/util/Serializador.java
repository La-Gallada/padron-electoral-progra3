package padron.util;

import java.util.List;
import padron.dto.DireccionDTO;
import padron.dto.ErrorRespuesta;
import padron.dto.PadronPageResponse;
import padron.dto.PersonaDTO;
import padron.dto.RespuestaPadron;

public final class Serializador {

    private Serializador() {}

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

    public static String toJson(PadronPageResponse r) {
        if (r == null) {
            return "{\"ok\":false,\"error\":{\"codigo\":\"NULL\",\"mensaje\":\"Respuesta nula\"}}";
        }

        StringBuilder sb = new StringBuilder(512);
        sb.append("{");
        sb.append("\"ok\":").append(r.isOk());

        if (r.isOk()) {
            sb.append(",\"criterio\":").append(Escapes.quoteJson(r.getCriterio()));
            sb.append(",\"paginaActual\":").append(r.getPaginaActual());
            sb.append(",\"tamanoPagina\":").append(r.getTamanoPagina());
            sb.append(",\"totalResultados\":").append(r.getTotalResultados());
            sb.append(",\"totalPaginas\":").append(r.getTotalPaginas());
            sb.append(",\"resultados\":").append(personasJson(r.getResultados()));
        } else {
            sb.append(",\"error\":").append(errorJson(r.getError()));
        }

        sb.append("}");
        return sb.toString();
    }

    public static String toXml(PadronPageResponse r) {
        if (r == null) {
            return "<pagina ok=\"false\"><error><codigo>NULL</codigo><mensaje>Respuesta nula</mensaje></error></pagina>";
        }

        StringBuilder sb = new StringBuilder(512);
        sb.append("<pagina ok=\"").append(r.isOk()).append("\">");

        if (r.isOk()) {
            sb.append("<criterio>").append(Escapes.xml(r.getCriterio())).append("</criterio>");
            sb.append("<paginaActual>").append(r.getPaginaActual()).append("</paginaActual>");
            sb.append("<tamanoPagina>").append(r.getTamanoPagina()).append("</tamanoPagina>");
            sb.append("<totalResultados>").append(r.getTotalResultados()).append("</totalResultados>");
            sb.append("<totalPaginas>").append(r.getTotalPaginas()).append("</totalPaginas>");
            sb.append("<resultados>");

            for (PersonaDTO p : r.getResultados()) {
                sb.append(personaXml(p));
            }

            sb.append("</resultados>");
        } else {
            sb.append(errorXml(r.getError()));
        }

        sb.append("</pagina>");
        return sb.toString();
    }

    private static String personasJson(List<PersonaDTO> personas) {
        if (personas == null || personas.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < personas.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(personaJson(personas.get(i)));
        }

        sb.append("]");
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