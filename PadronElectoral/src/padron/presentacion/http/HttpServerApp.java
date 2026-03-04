package padron.presentacion.http;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import padron.logica.ServicioPadron;

public class HttpServerApp {

    private final int port;
    private final int poolSize;
    private final ServicioPadron servicio;

    private HttpServer server;
    private ExecutorService executor;

    // ✅ Constructor que Main necesita
    public HttpServerApp(int port, int poolSize, ServicioPadron servicio) {
        this.port = port;
        this.poolSize = poolSize;
        this.servicio = servicio;
    }

    // (Opcional) constructor vacío si NetBeans lo generó
    public HttpServerApp() {
        this.port = 0;
        this.poolSize = 0;
        this.servicio = null;
    }

    public void start() throws IOException {
        if (server != null) return;

        server = HttpServer.create(new InetSocketAddress(port), 0);
        executor = Executors.newFixedThreadPool(poolSize > 0 ? poolSize : 10);
        server.setExecutor(executor);

        // Context principal
        server.createContext("/padron", new PadronHttpHandler(servicio));

        server.start();
        System.out.println("✅ HTTP escuchando en http://localhost:" + port + "/padron");
    }

    public void stop() {
        if (server != null) server.stop(0);
        if (executor != null) executor.shutdownNow();
    }
}