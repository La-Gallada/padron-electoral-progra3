package padron.presentacion.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpPadronClient {

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 180000;

    private final String baseUrl;

    public HttpPadronClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("La baseUrl no puede ser nula o vacía.");
        }

        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    public String consultarPorCedula(String cedula, String formato) throws IOException {
        String cedulaEncoded = URLEncoder.encode(
                cedula == null ? "" : cedula,
                StandardCharsets.UTF_8
        );
        String formatoEncoded = URLEncoder.encode(
                formato == null ? "json" : formato,
                StandardCharsets.UTF_8
        );

        String endpoint = baseUrl
                + "/padron?cedula=" + cedulaEncoded
                + "&format=" + formatoEncoded;

        return ejecutarGet(endpoint);
    }

    public String explorar(String criterio, int pagina, int tamano, String formato, String ordenarPor, String direccion) throws IOException {
        String criterioEncoded = URLEncoder.encode(
                criterio == null ? "" : criterio,
                StandardCharsets.UTF_8
        );
        String formatoEncoded = URLEncoder.encode(
                formato == null ? "json" : formato,
                StandardCharsets.UTF_8
        );
        String ordenarPorEncoded = URLEncoder.encode(
                ordenarPor == null ? "cedula" : ordenarPor,
                StandardCharsets.UTF_8
        );
        String direccionEncoded = URLEncoder.encode(
                direccion == null ? "asc" : direccion,
                StandardCharsets.UTF_8
        );

        String endpoint = baseUrl
                + "/padron/explorar?criterio=" + criterioEncoded
                + "&pagina=" + pagina
                + "&tamano=" + tamano
                + "&ordenarPor=" + ordenarPorEncoded
                + "&direccion=" + direccionEncoded
                + "&format=" + formatoEncoded;

        return ejecutarGet(endpoint);
    }

    public String explorar(String criterio, int pagina, int tamano, String formato) throws IOException {
        return explorar(criterio, pagina, tamano, formato, "cedula", "asc");
    }

    private String ejecutarGet(String endpoint) throws IOException {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            int status = connection.getResponseCode();

            InputStream stream = (status >= 200 && status < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String response = readStream(stream);

            if (status < 200 || status >= 300) {
                throw new IOException(
                        "HTTP " + status + " al consultar " + endpoint
                        + (response == null || response.isBlank() ? "" : "\nRespuesta: " + response)
                );
            }

            return response;

        } catch (SocketTimeoutException e) {
            throw new IOException(
                    "La consulta tardó demasiado en responder (timeout de " + READ_TIMEOUT_MS + " ms).",
                    e
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    sb.append(System.lineSeparator());
                }
                sb.append(line);
                firstLine = false;
            }
        }

        return sb.toString();
    }
}