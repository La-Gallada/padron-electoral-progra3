package padron.logica;

import padron.datos.RepositorioDistelec;
import padron.datos.RepositorioPadron;
import padron.dto.DireccionDTO;
import padron.dto.ErrorRespuesta;
import padron.dto.PersonaDTO;
import padron.dto.RespuestaPadron;
import padron.dto.SolicitudPadron;
import padron.entidades.Direccion;
import padron.entidades.Persona;
import padron.util.CedulaNormalizer;
import java.util.Optional;

public class ServicioPadron {

    private final RepositorioPadron   repoPadron;
    private final RepositorioDistelec repoDistelec;

    public ServicioPadron(RepositorioPadron repoPadron, RepositorioDistelec repoDistelec) {
        this.repoPadron   = repoPadron;
        this.repoDistelec = repoDistelec;
    }

    public ServicioPadron() {
        this.repoPadron   = null;
        this.repoDistelec = null;
    }

    public RespuestaPadron atender(SolicitudPadron solicitud) {

        // 1) Validar que la solicitud no sea nula
        if (solicitud == null) {
            return RespuestaPadron.error("SOLICITUD_INVALIDA", "La solicitud no puede ser nula");
        }

        // 2) Normalizar la cédula
        String cedulaNormalizada = CedulaNormalizer.normalizar(solicitud.getCedula());

        // 3) Validar la cédula normalizada
        if (!CedulaNormalizer.esValida(cedulaNormalizada)) {
            return RespuestaPadron.error("CEDULA_INVALIDA",
                    "La cédula no es válida: " + solicitud.getCedula());
        }

        // 4) Buscar persona en el padrón
        Optional<Persona> personaOpt;
        try {
            personaOpt = repoPadron.buscarPorCedula(cedulaNormalizada);
        } catch (Exception e) {
            return RespuestaPadron.error("ERROR_PADRON",
                    "Error consultando el padrón: " + e.getMessage());
        }

        // 5) Si no existe, devolver error
        if (personaOpt.isEmpty()) {
            return RespuestaPadron.error("CEDULA_NO_ENCONTRADA",
                    "No se encontró la cédula: " + cedulaNormalizada);
        }

        Persona persona = personaOpt.get();

        // 6) Buscar dirección en distelec usando codElec
        Optional<Direccion> direccionOpt;
        try {
            direccionOpt = repoDistelec.buscarPorCodElec(persona.getCodElec());
        } catch (Exception e) {
            return RespuestaPadron.error("ERROR_DISTELEC",
                    "Error consultando distelec: " + e.getMessage());
        }

        // 7) Construir PersonaDTO
        PersonaDTO personaDTO = new PersonaDTO(
            persona.getCedula(),
            persona.getNombre(),
            persona.getPrimerApellido(),
            persona.getSegundoApellido(),
            persona.getCodElec()
        );

        // 8) Construir DireccionDTO si existe dirección
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

        // 9) Devolver respuesta exitosa
        return RespuestaPadron.ok(personaDTO, direccionDTO);
    }
}