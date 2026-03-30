package padron.presentacion.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
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

        int status = connection.getResponseCode();

        BufferedReader reader;
        if (status >= 200 && status < 400) {
            reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );
        } else {
            reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)
            );
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append(System.lineSeparator());
        }

        reader.close();
        connection.disconnect();

        return sb.toString().trim();
    }
}