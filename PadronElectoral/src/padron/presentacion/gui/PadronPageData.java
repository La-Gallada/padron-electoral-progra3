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
    private final String ordenarPor;
    private final String direccion;
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
            String ordenarPor,
            String direccion,
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
        this.ordenarPor = ordenarPor;
        this.direccion = direccion;
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
            String ordenarPor,
            String direccion,
            List<PadronRow> resultados
    ) {
        return new PadronPageData(
                true,
                criterio == null ? "" : criterio,
                paginaActual,
                tamanoPagina,
                totalResultados,
                totalPaginas,
                ordenarPor == null ? "cedula" : ordenarPor,
                direccion == null ? "asc" : direccion,
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
                "cedula",
                "asc",
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

    public String getOrdenarPor() {
        return ordenarPor;
    }

    public String getDireccion() {
        return direccion;
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
