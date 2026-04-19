package padron.datos;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import padron.entidades.Direccion;

public class RepositorioDistelecTxt implements RepositorioDistelec {

    private final Path path;
    private final String sep;

    // Mapa cargado una sola vez en memoria
    private final Map<String, Direccion> mapa = new HashMap<>();

    public RepositorioDistelecTxt(Path path, String sep) {
        this.path = path;
        this.sep = sep;
    }

    @Override
    public void cargar() {

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {

            String linea;
            while ((linea = br.readLine()) != null) {

                String[] campos = linea.split("\\" + sep, -1);

                // Formato real: codElec,provincia,canton,distrito
                if (campos.length < 4) continue;

                String codElec = campos[0].trim();

                Direccion dir = new Direccion(
                    codElec,
                    campos[1].trim(),
                    campos[2].trim(),
                    campos[3].trim(),
                    ""
                );

                mapa.put(codElec, dir);
            }

            System.out.println(" Distelec cargado: " + mapa.size() + " registros");

        } catch (IOException e) {
            throw new RuntimeException("Error cargando distelec: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Direccion> buscarPorCodElec(String codElec) {
        if (codElec == null) return Optional.empty();
        return Optional.ofNullable(mapa.get(codElec.trim()));
    }
}