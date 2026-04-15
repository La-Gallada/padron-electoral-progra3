package padron.datos;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import padron.entidades.Persona;

public class RepositorioPadronTxt implements RepositorioPadron {

    private final Path path;
    private final String sep;

    private final List<Persona> personas = new ArrayList<>();
    private boolean cargado = false;

    public RepositorioPadronTxt(Path path, String sep) {
        this.path = path;
        this.sep = sep;
    }

    public synchronized void cargar() {
        if (cargado) {
            return;
        }

        personas.clear();

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\" + sep, -1);

                if (campos.length < 8) {
                    continue;
                }

                personas.add(crearPersona(campos));
            }

            cargado = true;
            System.out.println("✅ Padrón cargado en memoria: " + personas.size() + " registros");

        } catch (IOException e) {
            throw new RuntimeException("Error cargando padrón: " + e.getMessage(), e);
        }
    }

    private void asegurarCarga() {
        if (!cargado) {
            cargar();
        }
    }

    @Override
    public Optional<Persona> buscarPorCedula(String cedulaNormalizada) {
        asegurarCarga();

        for (Persona persona : personas) {
            if (persona.getCedula().equals(cedulaNormalizada)) {
                return Optional.of(persona);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Persona> listarPaginado(int offset, int limit) {
        return explorarPaginado("", offset, limit, "cedula", "asc");
    }

    @Override
    public int contarTotal() {
        asegurarCarga();
        return personas.size();
    }

    @Override
    public List<Persona> buscarPorNombrePaginado(String termino, int offset, int limit) {
        return explorarPaginado(termino, offset, limit, "cedula", "asc");
    }

    @Override
    public int contarPorNombre(String termino) {
        asegurarCarga();

        String terminoNormalizado = normalizarTexto(termino);
        String terminoSoloDigitos = soloDigitos(termino);
        int total = 0;

        for (Persona persona : personas) {
            if (coincide(persona, terminoNormalizado, terminoSoloDigitos)) {
                total++;
            }
        }

        return total;
    }

    @Override
    public List<Persona> explorarPaginado(String termino, int offset, int limit, String ordenarPor, String direccion) {
        asegurarCarga();

        List<Persona> resultados = new ArrayList<>();

        if (offset < 0 || limit <= 0) {
            return resultados;
        }

        String terminoNormalizado = normalizarTexto(termino);
        String terminoSoloDigitos = soloDigitos(termino);
        boolean sinFiltro = terminoNormalizado.isEmpty() && terminoSoloDigitos.isEmpty();

        int vistos = 0;

        for (Persona persona : personas) {
            boolean coincide = sinFiltro || coincide(persona, terminoNormalizado, terminoSoloDigitos);

            if (!coincide) {
                continue;
            }

            if (vistos >= offset && resultados.size() < limit) {
                resultados.add(persona);
            }

            vistos++;

            if (resultados.size() >= limit) {
                break;
            }
        }

        return resultados;
    }

    private boolean coincide(Persona persona, String terminoNormalizado, String terminoSoloDigitos) {
        String nombreCompleto = persona.getNombre() + " "
                + persona.getPrimerApellido() + " "
                + persona.getSegundoApellido();

        String nombreNormalizado = normalizarTexto(nombreCompleto);

        boolean coincideNombre = !terminoNormalizado.isEmpty()
                && nombreNormalizado.contains(terminoNormalizado);

        boolean coincideCedula = !terminoSoloDigitos.isEmpty()
                && persona.getCedula().contains(terminoSoloDigitos);

        return coincideNombre || coincideCedula;
    }

    private Persona crearPersona(String[] campos) {
        return new Persona(
                campos[0].trim(),
                campos[5].trim(),
                campos[6].trim(),
                campos[7].trim(),
                campos[1].trim()
        );
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return sinTildes.toLowerCase().trim();
    }

    private String soloDigitos(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replaceAll("\\D", "");
    }
}