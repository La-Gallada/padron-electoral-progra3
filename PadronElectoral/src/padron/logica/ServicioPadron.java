package padron.logica;

import padron.datos.RepositorioDistelec;
import padron.datos.RepositorioPadron;
import padron.dto.RespuestaPadron;
import padron.dto.SolicitudPadron;

public class ServicioPadron {

    private final RepositorioPadron repoPadron;
    private final RepositorioDistelec repoDistelec;

    // ✅ Constructor que Main necesita
    public ServicioPadron(RepositorioPadron repoPadron, RepositorioDistelec repoDistelec) {
        this.repoPadron = repoPadron;
        this.repoDistelec = repoDistelec;
    }

    // (Opcional) Constructor vacío por si NetBeans lo generó y lo quieren mantener
    public ServicioPadron() {
        this.repoPadron = null;
        this.repoDistelec = null;
    }

    // ✅ Firma mínima para el resto del sistema
    public RespuestaPadron atender(SolicitudPadron solicitud) {
        // luego aquí va la lógica real
        return null;
    }
}