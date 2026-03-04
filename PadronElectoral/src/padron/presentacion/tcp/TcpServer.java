package padron.presentacion.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import padron.logica.ServicioPadron;

public class TcpServer {

    private final int port;
    private final int poolSize;
    private final ServicioPadron servicio;

    private ServerSocket serverSocket;
    private ExecutorService pool;
    private volatile boolean running = false;

    // ✅ Constructor que Main necesita
    public TcpServer(int port, int poolSize, ServicioPadron servicio) {
        this.port = port;
        this.poolSize = poolSize;
        this.servicio = servicio;
    }

    // (Opcional) constructor vacío si NetBeans lo generó
    public TcpServer() {
        this.port = 0;
        this.poolSize = 0;
        this.servicio = null;
    }

    public void start() {
        if (running) return;

        running = true;
        pool = Executors.newFixedThreadPool(poolSize > 0 ? poolSize : 10);

        Thread acceptThread = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                serverSocket = ss;
                System.out.println("✅ TCP escuchando en puerto " + port);

                while (running) {
                    Socket client = ss.accept();
                    // Handler todavía puede estar vacío; lo creamos igual para compilar
                    pool.submit(new TcpClientHandler(client, servicio));
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("❌ Error en TCP server: " + e.getMessage());
                }
            }
        }, "tcp-accept-thread");

        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
        if (pool != null) pool.shutdownNow();
    }
}