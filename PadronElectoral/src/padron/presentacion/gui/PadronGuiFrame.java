package padron.presentacion.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

public class PadronGuiFrame extends JFrame {

    private final HttpPadronClient client;

    private final JTextField txtCedula;
    private final JComboBox<String> cmbFormato;
    private final JButton btnConsultar;
    private final JButton btnLimpiar;
    private final JButton btnCopiar;
    private final JTextArea txtResultado;
    private final JLabel lblEstado;

    public PadronGuiFrame() {
        this.client = new HttpPadronClient("http://localhost:8080");

        configurarVentana();

        JPanel contentPane = new JPanel(new BorderLayout(0, 12));
        contentPane.setBorder(new EmptyBorder(16, 16, 16, 16));
        contentPane.setBackground(new Color(245, 247, 250));

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                new EmptyBorder(10, 10, 10, 10)
        ));
        panelSuperior.setBackground(Color.WHITE);

        JLabel lblCedula = new JLabel("Cédula:");
        lblCedula.setFont(lblCedula.getFont().deriveFont(Font.BOLD, 14f));

        txtCedula = new JTextField(18);
        txtCedula.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JLabel lblFormato = new JLabel("Formato:");
        lblFormato.setFont(lblFormato.getFont().deriveFont(Font.BOLD, 14f));

        cmbFormato = new JComboBox<>(new String[]{"json", "xml"});
        cmbFormato.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        btnConsultar = createPrimaryButton("Consultar");
        btnLimpiar = new JButton("Limpiar");
        btnCopiar = new JButton("Copiar respuesta");

        btnLimpiar.setFocusPainted(false);
        btnCopiar.setFocusPainted(false);

        panelSuperior.add(lblCedula);
        panelSuperior.add(txtCedula);
        panelSuperior.add(lblFormato);
        panelSuperior.add(cmbFormato);
        panelSuperior.add(btnConsultar);
        panelSuperior.add(btnLimpiar);
        panelSuperior.add(btnCopiar);

        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtResultado.setLineWrap(false);
        txtResultado.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtResultado);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Respuesta del servidor"));
        scrollPane.setPreferredSize(new Dimension(760, 420));

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(new Color(245, 247, 250));
        panelInferior.setBorder(new EmptyBorder(0, 4, 0, 4));

        lblEstado = new JLabel("Estado: listo");
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 13f));
        lblEstado.setForeground(new Color(25, 118, 210));

        panelInferior.add(lblEstado, BorderLayout.WEST);

        contentPane.add(panelSuperior, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(panelInferior, BorderLayout.SOUTH);

        setContentPane(contentPane);

        btnConsultar.addActionListener(e -> consultar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnCopiar.addActionListener(e -> copiarRespuesta());
    }

    private void configurarVentana() {
        setTitle("Padrón Electoral - Demo GUI");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
    }

    private JButton createPrimaryButton(String texto) {
        JButton button = new JButton(texto);
        button.setFocusPainted(false);
        button.setBackground(new Color(33, 150, 243));
        button.setForeground(Color.WHITE);
        return button;
    }

    private void consultar() {
        String cedula = txtCedula.getText().trim();
        String formato = String.valueOf(cmbFormato.getSelectedItem()).trim();

        if (cedula.isEmpty()) {
            setEstadoError("error: cédula vacía");
            JOptionPane.showMessageDialog(
                    this,
                    "Ingresa una cédula antes de consultar.",
                    "Cédula requerida",
                    JOptionPane.WARNING_MESSAGE
            );
            txtCedula.requestFocusInWindow();
            return;
        }

        setConsultandoUI(true);
        setEstadoConsultando();

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return client.consultarPorCedula(cedula, formato);
            }

            @Override
            protected void done() {
                try {
                    String respuesta = get();
                    txtResultado.setText(respuesta);
                    txtResultado.setCaretPosition(0);
                    setEstadoListo("listo: respuesta recibida");
                } catch (Exception ex) {
                    txtResultado.setText("");
                    String mensaje = getMensajeAmigable(ex);
                    setEstadoError("error: backend no responde");
                    JOptionPane.showMessageDialog(
                            PadronGuiFrame.this,
                            mensaje,
                            "Error de conexión",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setConsultandoUI(false);
                }
            }
        };

        worker.execute();
    }

    private String getMensajeAmigable(Exception ex) {
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
        if (cause instanceof IOException) {
            return "No se pudo conectar con el backend en http://localhost:8080.\n"
                    + "Verifica que el servidor esté encendido e inténtalo de nuevo.";
        }
        return "Ocurrió un error inesperado al consultar el backend.\n"
                + "Detalle técnico: " + cause.getMessage();
    }

    private void limpiar() {
        txtCedula.setText("");
        cmbFormato.setSelectedIndex(0);
        txtResultado.setText("");
        setEstadoListo("listo");
        txtCedula.requestFocusInWindow();
    }

    private void copiarRespuesta() {
        String respuesta = txtResultado.getText();
        if (respuesta == null || respuesta.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay respuesta para copiar todavía.",
                    "Copiar respuesta",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(respuesta), null);
        setEstadoListo("listo: respuesta copiada");
    }

    private void setConsultandoUI(boolean consultando) {
        btnConsultar.setEnabled(!consultando);
        btnLimpiar.setEnabled(!consultando);
        btnCopiar.setEnabled(!consultando);
        txtCedula.setEnabled(!consultando);
        cmbFormato.setEnabled(!consultando);
    }

    private void setEstadoListo(String detalle) {
        lblEstado.setForeground(new Color(25, 118, 210));
        lblEstado.setText("Estado: " + detalle);
    }

    private void setEstadoConsultando() {
        lblEstado.setForeground(new Color(237, 108, 2));
        lblEstado.setText("Estado: consultando...");
    }

    private void setEstadoError(String detalle) {
        lblEstado.setForeground(new Color(198, 40, 40));
        lblEstado.setText("Estado: " + detalle);
    }
}