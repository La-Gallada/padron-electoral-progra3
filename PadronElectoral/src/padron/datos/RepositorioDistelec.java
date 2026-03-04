package padron.datos;

import padron.entidades.Direccion;
import java.util.Optional;

public interface RepositorioDistelec {
    void cargar();
    Optional<Direccion> buscarPorCodElec(String codElec);
}
