package padron.app;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {

    private AppConfig() {}

    // Rutas
    public static final Path PADRON_PATH = Paths.get("data", "PADRON.txt");
    public static final Path DISTELEC_PATH = Paths.get("data", "distelec.txt");

    // Puertos
    public static final int TCP_PORT = 5000;
    public static final int HTTP_PORT = 8080;

    // Concurrencia
    public static final int TCP_POOL_SIZE = 20;
    public static final int HTTP_POOL_SIZE = 20;

    // Separador de archivos TXT
    public static final String TXT_SEPARATOR = ",";

    // Cache
    public static final int PADRON_CACHE_SIZE = 500;
}