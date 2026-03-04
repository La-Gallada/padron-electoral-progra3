package padron.presentacion.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import padron.logica.ServicioPadron;

public class PadronHttpHandler implements HttpHandler {

    public PadronHttpHandler(ServicioPadron servicio) {
        // stub
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Respuesta temporal para confirmar que el server levanta
        String body = "PadronElectoral HTTP OK";
        exchange.sendResponseHeaders(200, body.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(body.getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }
}