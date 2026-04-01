package padron.logica;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import padron.datos.RepositorioDistelec;
import padron.datos.RepositorioPadron;
import padron.dto.DireccionDTO;
import padron.dto.PadronPageResponse;
import padron.dto.PersonaDTO;
import padron.dto.RespuestaPadron;
import padron.dto.SolicitudPadron;
import padron.entidades.Direccion;
import padron.entidades.Persona;
import padron.util.CedulaNormalizer;

public class ServicioPadron {

    private final RepositorioPadron repoPadron;
    private final RepositorioDistelec repoDistelec;

    public ServicioPadron(RepositorioPadron repoPadron, RepositorioDistelec repoDistelec) {
        this.repoPadron = repoPadron;
        this.repoDistelec = repoDistelec;
    }

    public ServicioPadron() {
        this.repoPadron = null;
        this.repoDistelec = null;
    }

    public RespuestaPadron atender(SolicitudPadron solicitud) {
        if (solicitud == null) {
            return RespuestaPadron.error("SOLICITUD_INVALIDA", "La solicitud no puede ser nula");
        }

        String cedulaNormalizada = CedulaNormalizer.normalizar(solicitud.getCedula());

        if (!CedulaNormalizer.esValida(cedulaNormalizada)) {
            return RespuestaPadron.error(
                "CEDULA_INVALIDA",
                "La cédula no es válida: " + solicitud.getCedula()
            );
        }

        Optional<Persona> personaOpt;

        try {
            personaOpt = repoPadron.buscarPorCedula(cedulaNormalizada);
        } catch (Exception e) {
            return RespuestaPadron.error(
                "ERROR_PADRON",
                "Error consultando el padrón: " + e.getMessage()
            );
        }

        if (personaOpt.isEmpty()) {
            return RespuestaPadron.error(
                "CEDULA_NO_ENCONTRADA",
                "No se encontró la cédula: " + cedulaNormalizada
            );
        }

        Persona persona = personaOpt.get();
        Optional<Direccion> direccionOpt;

        try {
            direccionOpt = repoDistelec.buscarPorCodElec(persona.getCodElec());
        } catch (Exception e) {
            return RespuestaPadron.error(
                "ERROR_DISTELEC",
                "Error consultando distelec: " + e.getMessage()
            );
        }

        PersonaDTO personaDTO = new PersonaDTO(
            persona.getCedula(),
            persona.getNombre(),
            persona.getPrimerApellido(),
            persona.getSegundoApellido(),
            persona.getCodElec()
        );

        DireccionDTO direccionDTO = null;

        if (direccionOpt.isPresent()) {
            Direccion dir = direccionOpt.get();
            direccionDTO = new DireccionDTO(
                dir.getProvincia(),
                dir.getCanton(),
                dir.getDistrito(),
                dir.getRecinto()
            );
        }

        return RespuestaPadron.ok(personaDTO, direccionDTO);
    }

    public PadronPageResponse explorar(String criterio, int pagina, int tamano) {
        if (pagina < 1) {
            return PadronPageResponse.error(
                "PAGINA_INVALIDA",
                "La página debe ser mayor o igual a 1."
            );
        }

        if (tamano < 1 || tamano > 500) {
            return PadronPageResponse.error(
                "TAMANO_INVALIDO",
                "El tamaño de página debe estar entre 1 y 500."
            );
        }

        try {
            String criterioLimpio = criterio == null ? "" : criterio.trim();
            boolean filtrado = !criterioLimpio.isEmpty();

            int totalResultados = filtrado
                ? repoPadron.contarPorNombre(criterioLimpio)
                : repoPadron.contarTotal();

            int totalPaginas = totalResultados == 0
                ? 1
                : (int) Math.ceil((double) totalResultados / tamano);

            int paginaAUsar = Math.min(pagina, totalPaginas);
            int offset = (paginaAUsar - 1) * tamano;

            List<Persona> personas = filtrado
                ? repoPadron.buscarPorNombrePaginado(criterioLimpio, offset, tamano)
                : repoPadron.listarPaginado(offset, tamano);

            List<PersonaDTO> resultados = new ArrayList<>();

            for (Persona persona : personas) {
                resultados.add(new PersonaDTO(
                    persona.getCedula(),
                    persona.getNombre(),
                    persona.getPrimerApellido(),
                    persona.getSegundoApellido(),
                    persona.getCodElec()
                ));
            }

            return PadronPageResponse.ok(
                resultados,
                totalResultados,
                paginaAUsar,
                tamano,
                totalPaginas,
                criterioLimpio
            );
        } catch (Exception e) {
            return PadronPageResponse.error(
                "ERROR_EXPLORACION",
                "Error explorando el padrón: " + e.getMessage()
            );
        }
    }
}