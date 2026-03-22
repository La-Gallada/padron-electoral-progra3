package padron.datos;

import java.nio.file.Path;
import padron.entidades.Persona;
import java.util.Optional;

public class RepositorioPadronTxt implements RepositorioPadron {

    public RepositorioPadronTxt(Path path, String sep) {}

    @Override
    public Optional<Persona> buscarPorCedula(String cedulaNormalizada) {
        return Optional.empty();
    }
}