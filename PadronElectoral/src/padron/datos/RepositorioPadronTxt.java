package padron.datos;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import padron.entidades.Persona;

public class RepositorioPadronTxt implements RepositorioPadron {

    private final Path   path;
    private final String sep;

    public RepositorioPadronTxt(Path path, String sep) {
        this.path = path;
        this.sep  = sep;
    }

    @Override
    public Optional<Persona> buscarPorCedula(String cedulaNormalizada) {

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {

            String linea;
            while ((linea = br.readLine()) != null) {

                String[] campos = linea.split("\\" + sep, -1);

                // Formato esperado: cedula|nombre|primerApellido|segundoApellido|codElec
                if (campos.length < 5) continue;

                String cedulaArchivo = campos[0].trim();

                if (cedulaArchivo.equals(cedulaNormalizada)) {
                    Persona persona = new Persona(
                        cedulaArchivo,
                        campos[1].trim(),
                        campos[2].trim(),
                        campos[3].trim(),
                        campos[4].trim()
                    );
                    return Optional.of(persona);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo padrón: " + e.getMessage(), e);
        }

        return Optional.empty();
    }
}