package padron.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PadronPageResponse {

    private final boolean ok;
    private final List<PersonaDTO> resultados;
    private final int totalResultados;
    private final int paginaActual;
    private final int tamanoPagina;
    private final int totalPaginas;
    private final String criterio;
    private final String ordenarPor;
    private final String direccion;
    private final ErrorRespuesta error;

    private PadronPageResponse(
            boolean ok,
            List<PersonaDTO> resultados,
            int totalResultados,
            int paginaActual,
            int tamanoPagina,
            int totalPaginas,
            String criterio,
            String ordenarPor,
            String direccion,
            ErrorRespuesta error
    ) {
        this.ok = ok;
        this.resultados = Collections.unmodifiableList(new ArrayList<>(resultados));
        this.totalResultados = totalResultados;
        this.paginaActual = paginaActual;
        this.tamanoPagina = tamanoPagina;
        this.totalPaginas = totalPaginas;
        this.criterio = criterio;
        this.ordenarPor = ordenarPor;
        this.direccion = direccion;
        this.error = error;
    }

    public static PadronPageResponse ok(
            List<PersonaDTO> resultados,
            int totalResultados,
            int paginaActual,
            int tamanoPagina,
            int totalPaginas,
            String criterio,
            String ordenarPor,
            String direccion
    ) {
        return new PadronPageResponse(
                true,
                resultados == null ? Collections.emptyList() : resultados,
                totalResultados,
                paginaActual,
                tamanoPagina,
                totalPaginas,
                criterio == null ? "" : criterio,
                ordenarPor == null ? "cedula" : ordenarPor,
                direccion == null ? "asc" : direccion,
                null
        );
    }

    public static PadronPageResponse error(String codigo, String mensaje) {
        return new PadronPageResponse(
                false,
                Collections.emptyList(),
                0,
                1,
                0,
                0,
                "",
                "cedula",
                "asc",
                new ErrorRespuesta(codigo, mensaje)
        );
    }

    public boolean isOk() {
        return ok;
    }

    public List<PersonaDTO> getResultados() {
        return resultados;
    }

    public int getTotalResultados() {
        return totalResultados;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public int getTamanoPagina() {
        return tamanoPagina;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public String getCriterio() {
        return criterio;
    }

    public String getOrdenarPor() {
        return ordenarPor;
    }

    public String getDireccion() {
        return direccion;
    }

    public ErrorRespuesta getError() {
        return error;
    }
}