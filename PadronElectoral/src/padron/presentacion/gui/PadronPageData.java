package padron.presentacion.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PadronPageData {

    private final boolean ok;
    private final String criterio;
    private final int paginaActual;
    private final int tamanoPagina;
    private final int totalResultados;
    private final int totalPaginas;
    private final List<PadronRow> resultados;
    private final String errorCodigo;
    private final String errorMensaje;

    private PadronPageData(
            boolean ok,
            String criterio,
            int paginaActual,
            int tamanoPagina,
            int totalResultados,
            int totalPaginas,
            List<PadronRow> resultados,
            String errorCodigo,
            String errorMensaje
    ) {
        this.ok = ok;
        this.criterio = criterio;
        this.paginaActual = paginaActual;
        this.tamanoPagina = tamanoPagina;
        this.totalResultados = totalResultados;
        this.totalPaginas = totalPaginas;
        this.resultados = Collections.unmodifiableList(new ArrayList<>(resultados));
        this.errorCodigo = errorCodigo;
        this.errorMensaje = errorMensaje;
    }

    public static PadronPageData ok(
            String criterio,
            int paginaActual,
            int tamanoPagina,
            int totalResultados,
            int totalPaginas,
            List<PadronRow> resultados
    ) {
        return new PadronPageData(
                true,
                criterio == null ? "" : criterio,
                paginaActual,
                tamanoPagina,
                totalResultados,
                totalPaginas,
                resultados == null ? Collections.emptyList() : resultados,
                null,
                null
        );
    }

    public static PadronPageData error(String codigo, String mensaje) {
        return new PadronPageData(
                false,
                "",
                1,
                0,
                0,
                0,
                Collections.emptyList(),
                codigo,
                mensaje
        );
    }

    public boolean isOk() {
        return ok;
    }

    public String getCriterio() {
        return criterio;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public int getTamanoPagina() {
        return tamanoPagina;
    }

    public int getTotalResultados() {
        return totalResultados;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public List<PadronRow> getResultados() {
        return resultados;
    }

    public String getErrorCodigo() {
        return errorCodigo;
    }

    public String getErrorMensaje() {
        return errorMensaje;
    }
}
