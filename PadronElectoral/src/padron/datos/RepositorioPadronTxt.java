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

    public RepositorioPadronTxt(Path path, String sep) {
        this.path = path;
        this.sep = sep;
    }

    @Override
    public Optional<Persona> buscarPorCedula(String cedulaNormalizada) {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\" + sep, -1);

                // Formato real:
                // [0]=cedula, [1]=codElec, [5]=nombre, [6]=primerApellido, [7]=segundoApellido
                if (campos.length < 8) {
                    continue;
                }

                String cedulaArchivo = campos[0].trim();

                if (cedulaArchivo.equals(cedulaNormalizada)) {
                    return Optional.of(crearPersona(campos));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo padrón: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Persona> listarPaginado(int offset, int limit) {
        List<Persona> resultados = new ArrayList<>();

        if (offset < 0 || limit <= 0) {
            return resultados;
        }

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;
            int indiceValido = 0;

            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\" + sep, -1);

                if (campos.length < 8) {
                    continue;
                }

                if (indiceValido >= offset && resultados.size() < limit) {
                    resultados.add(crearPersona(campos));
                }

                indiceValido++;

                if (resultados.size() >= limit) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error listando padrón: " + e.getMessage(), e);
        }

        return resultados;
    }

    @Override
    public int contarTotal() {
        int total = 0;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\" + sep, -1);

                if (campos.length >= 8) {
                    total++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error contando padrón: " + e.getMessage(), e);
        }

        return total;
    }

    @Override
    public List<Persona> buscarPorNombrePaginado(String termino, int offset, int limit) {
        List<Persona> resultados = new ArrayList<>();

        if (offset < 0 || limit <= 0) {
            return resultados;
        }

        String terminoNormalizado = normalizarTexto(termino);
        String terminoSoloDigitos = soloDigitos(termino);

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;
            int indiceCoincidencia = 0;

            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\" + sep, -1);

                if (campos.length < 8) {
                    continue;
                }

                String cedula = campos[0].trim();
                String nombreCompleto = construirNombreCompleto(campos);
                String nombreNormalizado = normalizarTexto(nombreCompleto);

                boolean coincideNombre = nombreNormalizado.contains(terminoNormalizado);
                boolean coincideCedula = !terminoSoloDigitos.isEmpty() && cedula.contains(terminoSoloDigitos);

                if (coincideNombre || coincideCedula) {
                    if (indiceCoincidencia >= offset && resultados.size() < limit) {
                        resultados.add(crearPersona(campos));
                    }

                    indiceCoincidencia++;
                }

                if (resultados.size() >= limit) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error buscando por nombre o cédula en padrón: " + e.getMessage(), e);
        }

        return resultados;
    }

    @Override
    public int contarPorNombre(String termino) {
        String terminoNormalizado = normalizarTexto(termino);
        String terminoSoloDigitos = soloDigitos(termino);
        int total = 0;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\" + sep, -1);

                if (campos.length < 8) {
                    continue;
                }

                String cedula = campos[0].trim();
                String nombreCompleto = construirNombreCompleto(campos);
                String nombreNormalizado = normalizarTexto(nombreCompleto);

                boolean coincideNombre = nombreNormalizado.contains(terminoNormalizado);
                boolean coincideCedula = !terminoSoloDigitos.isEmpty() && cedula.contains(terminoSoloDigitos);

                if (coincideNombre || coincideCedula) {
                    total++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error contando resultados por nombre o cédula: " + e.getMessage(), e);
        }

        return total;
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

    private String construirNombreCompleto(String[] campos) {
        return campos[5].trim() + " " + campos[6].trim() + " " + campos[7].trim();
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