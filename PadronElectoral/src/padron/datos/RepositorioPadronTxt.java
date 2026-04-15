package padron.datos;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import padron.entidades.Persona;

public class RepositorioPadronTxt implements RepositorioPadron {

    private static final int MAX_COUNT_CACHE = 50;
    private static final int MAX_PAGE_CACHE = 100;

    private final Path path;
    private final String sep;
    private final Pattern splitPattern;

    private Integer totalRegistrosCache = null;

    private final Map<String, Integer> countCache = new LinkedHashMap<String, Integer>(MAX_COUNT_CACHE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return size() > MAX_COUNT_CACHE;
        }
    };

    private final Map<String, List<Persona>> pageCache = new LinkedHashMap<String, List<Persona>>(MAX_PAGE_CACHE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<Persona>> eldest) {
            return size() > MAX_PAGE_CACHE;
        }
    };

    public RepositorioPadronTxt(Path path, String sep) {
        this.path = path;
        this.sep = sep;
        this.splitPattern = Pattern.compile(Pattern.quote(sep));
    }

    public synchronized void cargar() {
        if (totalRegistrosCache != null) {
            return;
        }

        totalRegistrosCache = contarRegistrosArchivo();
        System.out.println("✅ Conteo base del padrón listo: " + totalRegistrosCache + " registros");
    }

    @Override
    public Optional<Persona> buscarPorCedula(String cedulaNormalizada) {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] campos = splitPattern.split(linea, -1);

                if (!esRegistroValido(campos)) {
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
        return explorarPaginado("", offset, limit, "cedula", "asc");
    }

    @Override
    public int contarTotal() {
        if (totalRegistrosCache != null) {
            return totalRegistrosCache;
        }

        synchronized (this) {
            if (totalRegistrosCache == null) {
                totalRegistrosCache = contarRegistrosArchivo();
            }
            return totalRegistrosCache;
        }
    }

    @Override
    public List<Persona> buscarPorNombrePaginado(String termino, int offset, int limit) {
        return explorarPaginado(termino, offset, limit, "cedula", "asc");
    }

    @Override
    public int contarPorNombre(String termino) {
        String terminoNormalizado = normalizarTexto(termino);
        String terminoSoloDigitos = soloDigitos(termino);
        String cacheKey = terminoNormalizado + "|" + terminoSoloDigitos;

        synchronized (countCache) {
            Integer cached = countCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        int total = 0;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] campos = splitPattern.split(linea, -1);

                if (!esRegistroValido(campos)) {
                    continue;
                }

                String cedula = campos[0].trim();
                String nombreCompleto = construirNombreCompleto(campos);
                String nombreNormalizado = normalizarTexto(nombreCompleto);

                boolean coincideNombre = !terminoNormalizado.isEmpty()
                        && nombreNormalizado.contains(terminoNormalizado);

                boolean coincideCedula = !terminoSoloDigitos.isEmpty()
                        && cedula.contains(terminoSoloDigitos);

                if (coincideNombre || coincideCedula) {
                    total++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error contando resultados por nombre o cédula: " + e.getMessage(), e);
        }

        synchronized (countCache) {
            countCache.put(cacheKey, total);
        }

        return total;
    }

    @Override
    public List<Persona> explorarPaginado(String termino, int offset, int limit, String ordenarPor, String direccion) {
        String terminoNormalizado = normalizarTexto(termino);
        String terminoSoloDigitos = soloDigitos(termino);

        String cacheKey = terminoNormalizado + "|" + terminoSoloDigitos + "|" + offset + "|" + limit;

        synchronized (pageCache) {
            List<Persona> cached = pageCache.get(cacheKey);
            if (cached != null) {
                return new ArrayList<>(cached);
            }
        }

        List<Persona> resultados = new ArrayList<>();

        if (offset < 0 || limit <= 0) {
            return resultados;
        }

        boolean sinFiltro = terminoNormalizado.isEmpty() && terminoSoloDigitos.isEmpty();

        if (sinFiltro) {
            resultados = listarPaginadoDesdeArchivo(offset, limit);
        } else {
            resultados = buscarPaginadoDesdeArchivo(terminoNormalizado, terminoSoloDigitos, offset, limit);
        }

        synchronized (pageCache) {
            pageCache.put(cacheKey, new ArrayList<>(resultados));
        }

        return resultados;
    }

    private List<Persona> listarPaginadoDesdeArchivo(int offset, int limit) {
        List<Persona> resultados = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;
            int indiceValido = 0;

            while ((linea = br.readLine()) != null) {
                String[] campos = splitPattern.split(linea, -1);

                if (!esRegistroValido(campos)) {
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
            throw new RuntimeException("Error listando padrón paginado: " + e.getMessage(), e);
        }

        return resultados;
    }

    private List<Persona> buscarPaginadoDesdeArchivo(String terminoNormalizado, String terminoSoloDigitos, int offset, int limit) {
        List<Persona> resultados = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;
            int indiceCoincidencia = 0;

            while ((linea = br.readLine()) != null) {
                String[] campos = splitPattern.split(linea, -1);

                if (!esRegistroValido(campos)) {
                    continue;
                }

                String cedula = campos[0].trim();
                String nombreCompleto = construirNombreCompleto(campos);
                String nombreNormalizado = normalizarTexto(nombreCompleto);

                boolean coincideNombre = !terminoNormalizado.isEmpty()
                        && nombreNormalizado.contains(terminoNormalizado);

                boolean coincideCedula = !terminoSoloDigitos.isEmpty()
                        && cedula.contains(terminoSoloDigitos);

                if (coincideNombre || coincideCedula) {
                    if (indiceCoincidencia >= offset && resultados.size() < limit) {
                        resultados.add(crearPersona(campos));
                    }

                    indiceCoincidencia++;

                    if (resultados.size() >= limit) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error explorando padrón: " + e.getMessage(), e);
        }

        return resultados;
    }

    private int contarRegistrosArchivo() {
        int total = 0;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            String linea;

            while ((linea = br.readLine()) != null) {
                String[] campos = splitPattern.split(linea, -1);
                if (esRegistroValido(campos)) {
                    total++;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error contando padrón: " + e.getMessage(), e);
        }

        return total;
    }

    private boolean esRegistroValido(String[] campos) {
        return campos != null && campos.length >= 8;
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