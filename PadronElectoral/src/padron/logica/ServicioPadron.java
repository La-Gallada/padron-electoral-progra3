package padron.logica;

import padron.datos.RepositorioDistelec;
import padron.datos.RepositorioPadron;
import padron.dto.DireccionDTO;
import padron.dto.PersonaDTO;
import padron.dto.RespuestaPadron;
import padron.dto.SolicitudPadron;
import padron.entidades.Direccion;
import padron.entidades.Persona;
import padron.util.CedulaNormalizer;
import java.util.Optional;

public class ServicioPadron {

    private final RepositorioPadron    repoPadron;
    private final RepositorioDistelec  repoDistelec;

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
            return new RespuestaPadron("Solicitud inválida");
        }

        // 2) Normalizar la cédula
        String cedulaNormalizada = CedulaNormalizer.normalizar(solicitud.getCedula());

        // 3) Validar la cédula normalizada
        if (!CedulaNormalizer.esValida(cedulaNormalizada)) {
            return new RespuestaPadron("Cédula inválida: " + solicitud.getCedula());
        }

        // 4) Buscar persona en el padrón
        Optional<Persona> personaOpt;
        try {
            personaOpt = repoPadron.buscarPorCedula(cedulaNormalizada);
        } catch (Exception e) {
            return new RespuestaPadron("Error consultando el padrón: " + e.getMessage());
        }

        if (personaOpt.isEmpty()) {
            return new RespuestaPadron("Cédula no encontrada: " + cedulaNormalizada);
        }

        Persona persona = personaOpt.get();

        // 5) Buscar dirección en distelec usando el codElec de la persona
        Optional<Direccion> direccionOpt;
        try {
            direccionOpt = repoDistelec.buscarPorCodElec(persona.getCodElec());
        } catch (Exception e) {
            return new RespuestaPadron("Error consultando distelec: " + e.getMessage());
        }

        // 6) Construir DTOs
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

        // 7) Retornar respuesta exitosa
        return new RespuestaPadron(personaDTO, direccionDTO);
    }
}