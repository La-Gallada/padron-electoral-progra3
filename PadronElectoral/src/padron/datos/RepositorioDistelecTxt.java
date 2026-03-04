package padron.datos;

import java.nio.file.Path;
import padron.entidades.Direccion;
import java.util.Optional;

public class RepositorioDistelecTxt implements RepositorioDistelec {

    public RepositorioDistelecTxt(Path path, String sep) {}

    @Override
    public void cargar() {}

    @Override
    public Optional<Direccion> buscarPorCodElec(String codElec) {
        return Optional.empty();
    }
}