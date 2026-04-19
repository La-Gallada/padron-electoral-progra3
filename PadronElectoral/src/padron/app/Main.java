package padron.app;

import javax.swing.SwingUtilities;
import padron.datos.RepositorioDistelec;
import padron.datos.RepositorioDistelecTxt;
import padron.datos.RepositorioPadron;
import padron.datos.RepositorioPadronTxt;
import padron.logica.ServicioPadron;
import padron.presentacion.gui.PadronGuiFrame;
import padron.presentacion.http.HttpServerApp;
import padron.presentacion.tcp.TcpServer;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            RepositorioDistelec repoDistelec = new RepositorioDistelecTxt(
                    AppConfig.DISTELEC_PATH,
                    AppConfig.TXT_SEPARATOR
            );
            repoDistelec.cargar();

            RepositorioPadron repoPadron = new RepositorioPadronTxt(
                    AppConfig.PADRON_PATH,
                    AppConfig.TXT_SEPARATOR
            );

            ServicioPadron servicio = new ServicioPadron(repoPadron, repoDistelec);

            TcpServer tcp = new TcpServer(
                    AppConfig.TCP_PORT,
                    AppConfig.TCP_POOL_SIZE,
                    servicio
            );

            HttpServerApp http = new HttpServerApp(
                    AppConfig.HTTP_PORT,
                    AppConfig.HTTP_POOL_SIZE,
                    servicio
            );

            tcp.start();
            http.start();

            System.out.println("✅ PadronElectoral iniciado");
            System.out.println("   TCP  : localhost:" + AppConfig.TCP_PORT);
            System.out.println("   HTTP : http://localhost:" + AppConfig.HTTP_PORT);

            SwingUtilities.invokeLater(() -> {
                PadronGuiFrame gui = new PadronGuiFrame();
                gui.setVisible(true);
            });

        } catch (Exception ex) {
            System.err.println("❌ Error iniciando PadronElectoral: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}