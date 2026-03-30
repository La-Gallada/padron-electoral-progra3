package padron.presentacion.gui;

import javax.swing.SwingUtilities;

public class GuiMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PadronGuiFrame frame = new PadronGuiFrame();
            frame.setVisible(true);
        });
    }
}
