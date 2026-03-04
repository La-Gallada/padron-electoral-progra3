package padron.datos;

import padron.entidades.Persona;
import java.util.Optional;

public interface RepositorioPadron {
    Optional<Persona> buscarPorCedula(String cedulaNormalizada);
}
