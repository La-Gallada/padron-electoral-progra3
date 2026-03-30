package padron.presentacion.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpPadronClient {

    private final String baseUrl;

    public HttpPadronClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String consultarPorCedula(String cedula, String formato) throws IOException {
        String cedulaEncoded = URLEncoder.encode(cedula, StandardCharsets.UTF_8);
        String formatoEncoded = URLEncoder.encode(formato, StandardCharsets.UTF_8);

        String endpoint = baseUrl + "/padron?cedula=" + cedulaEncoded + "&format=" + formatoEncoded;
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        InputStream stream;
        int status = connection.getResponseCode();
        if (status >= 200 && status < 400) {
            stream = connection.getInputStream();
        } else {
            stream = connection.getErrorStream();
        }

        String response = readStream(stream);
        connection.disconnect();
        return response;
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
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