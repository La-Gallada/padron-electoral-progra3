package padron.presentacion.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GuiMain {

    public static void main(String[] args) {
        aplicarLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            PadronGuiFrame frame = new PadronGuiFrame();
            frame.setVisible(true);
        });
    }

    private static void aplicarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // Si falla, Swing usa el look and feel por defecto.
        }
    }
}