package padron.presentacion.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class PadronGuiFrame extends JFrame {

    private final HttpPadronClient client;

    private final JTextField txtCedula;
    private final JComboBox<String> cmbFormato;
    private final JButton btnBuscar;
    private final JButton btnLimpiar;
    private final JTextArea txtResultado;

    public PadronGuiFrame() {
        this.client = new HttpPadronClient("http://localhost:8080");

        setTitle("PadronElectoral - Cliente GUI");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(760, 500));
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Consulta"));

        JLabel lblCedula = new JLabel("Cédula:");
        txtCedula = new JTextField(18);

        JLabel lblFormato = new JLabel("Formato:");
        cmbFormato = new JComboBox<>(new String[]{"json", "xml"});

        btnBuscar = new JButton("Buscar");
        btnLimpiar = new JButton("Limpiar");

        topPanel.add(lblCedula);
        topPanel.add(txtCedula);
        topPanel.add(lblFormato);
        topPanel.add(cmbFormato);
        topPanel.add(btnBuscar);
        topPanel.add(btnLimpiar);

        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(txtResultado);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Respuesta del servidor"));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
    }

    private void buscar() {
        String cedula = txtCedula.getText().trim();
        String formato = String.valueOf(cmbFormato.getSelectedItem()).trim();

        if (cedula.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debes ingresar una cédula.",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        txtResultado.setText("Consultando servidor...");

        btnBuscar.setEnabled(false);

        Thread hilo = new Thread(() -> {
            try {
                String respuesta = client.consultarPorCedula(cedula, formato);

                SwingUtilities.invokeLater(() -> txtResultado.setText(respuesta));

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    txtResultado.setText("");
                    JOptionPane.showMessageDialog(
                            this,
                            "No se pudo consultar el servidor.\n\nDetalle: " + ex.getMessage()
                                    + "\n\nAsegúrate de que el backend esté levantado en http://localhost:8080",
                            "Error de conexión",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnBuscar.setEnabled(true));
            }
        });

        hilo.start();
    }

    private void limpiar() {
        txtCedula.setText("");
        txtResultado.setText("");
        cmbFormato.setSelectedIndex(0);
    }
}
