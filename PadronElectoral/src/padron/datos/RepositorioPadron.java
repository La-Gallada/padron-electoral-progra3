package padron.datos;

import java.util.List;
import java.util.Optional;
import padron.entidades.Persona;

public interface RepositorioPadron {

    Optional<Persona> buscarPorCedula(String cedulaNormalizada);

    List<Persona> listarPaginado(int offset, int limit);

    int contarTotal();

    List<Persona> buscarPorNombrePaginado(String termino, int offset, int limit);

    int contarPorNombre(String termino);

    List<Persona> explorarPaginado(String termino, int offset, int limit, String ordenarPor, String direccion);
}