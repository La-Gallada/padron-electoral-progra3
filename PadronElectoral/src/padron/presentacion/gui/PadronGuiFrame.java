package padron.presentacion.gui;

import com.itextpdf.text.DocumentException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PadronGuiFrame extends JFrame {

    private static final int TAMANO_PAGINA = 100;

    private final HttpPadronClient client;
    private final PdfExportService pdfExportService;

    private final JTextField txtBusqueda;
    private final JButton btnBuscar;
    private final JButton btnLimpiarBusqueda;
    private final JButton btnAnterior;
    private final JButton btnSiguiente;
    private final JButton btnAgregarSeleccionado;

    private final JTable tblPadron;
    private final PadronTableModel padronTableModel;
    private final JLabel lblResumenPadron;

    private final JTable tblSeleccionados;
    private final SeleccionadosTableModel seleccionadosTableModel;
    private final JButton btnQuitarSeleccionado;
    private final JButton btnLimpiarLista;
    private final JButton btnExportarPdf;
    private final JComboBox<String> cmbFormatoDetalle;
    private final JLabel lblResumenSeleccionados;

    private final JLabel lblEstado;

    private final Map<String, ConsultaSeleccionada> seleccionadosMap = new LinkedHashMap<>();

    private int paginaActual = 1;
    private int totalPaginas = 1;
    private String criterioActual = "";

    public PadronGuiFrame() {
        this.client = new HttpPadronClient("http://localhost:8080");
        this.pdfExportService = new PdfExportService();

        setTitle("Padrón Electoral - Explorador y Exportación PDF");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));

        padronTableModel = new PadronTableModel();
        seleccionadosTableModel = new SeleccionadosTableModel();

        txtBusqueda = new JTextField(26);
        btnBuscar = new JButton("Buscar");
        btnLimpiarBusqueda = new JButton("Limpiar");

        btnAnterior = new JButton("Anterior");
        btnSiguiente = new JButton("Siguiente");
        btnAgregarSeleccionado = new JButton("Agregar seleccionado");

        tblPadron = new JTable(padronTableModel);
        tblSeleccionados = new JTable(seleccionadosTableModel);

        btnQuitarSeleccionado = new JButton("Quitar seleccionado");
        btnLimpiarLista = new JButton("Limpiar lista");
        btnExportarPdf = new JButton("Exportar PDF");
        cmbFormatoDetalle = new JComboBox<>(new String[]{"JSON", "XML"});

        lblResumenPadron = new JLabel("Mostrando 0-0 de 0 registros | Página 1 de 1");
        lblResumenSeleccionados = new JLabel("Seleccionados: 0");
        lblEstado = new JLabel("Estado: listo");

        construirUI();
        configurarEventos();

        pack();
        setSize(1260, 760);
        setLocationRelativeTo(null);

        cargarPagina(1);
    }

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(new Color(245, 247, 250));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                construirPanelIzquierdo(),
                construirPanelDerecho()
        );
        splitPane.setResizeWeight(0.68);

        JPanel panelEstado = new JPanel(new BorderLayout());
        panelEstado.setBackground(new Color(245, 247, 250));
        panelEstado.setBorder(new EmptyBorder(4, 4, 0, 4));

        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 13f));
        lblEstado.setForeground(new Color(25, 118, 210));

        panelEstado.add(lblEstado, BorderLayout.WEST);

        root.add(splitPane, BorderLayout.CENTER);
        root.add(panelEstado, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel construirPanelIzquierdo() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titulo = new JLabel("Padrón / Búsqueda");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));

        JLabel ayuda = new JLabel("Deja el campo vacío para explorar todo el padrón o escribe nombre/cédula para filtrar.");
        ayuda.setForeground(new Color(90, 90, 90));

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);
        topInfo.add(titulo, BorderLayout.NORTH);
        topInfo.add(ayuda, BorderLayout.SOUTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelBusqueda.setOpaque(false);
        panelBusqueda.add(new JLabel("Nombre o cédula:"));
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnLimpiarBusqueda);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(topInfo, BorderLayout.NORTH);
        top.add(panelBusqueda, BorderLayout.SOUTH);

        tblPadron.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPadron.setRowHeight(24);
        tblPadron.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(tblPadron);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultados"));

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nav.setOpaque(false);
        nav.add(btnAnterior);
        nav.add(btnSiguiente);
        nav.add(btnAgregarSeleccionado);

        bottom.add(lblResumenPadron, BorderLayout.NORTH);
        bottom.add(nav, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel construirPanelDerecho() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titulo = new JLabel("Lista seleccionada para PDF");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));

        tblSeleccionados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSeleccionados.setRowHeight(24);
        tblSeleccionados.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(tblSeleccionados);
        scroll.setBorder(BorderFactory.createTitledBorder("Seleccionados"));

        JPanel formatoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        formatoPanel.setOpaque(false);
        formatoPanel.add(new JLabel("Formato detalle PDF:"));
        formatoPanel.add(cmbFormatoDetalle);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnQuitarSeleccionado);
        botones.add(btnLimpiarLista);
        botones.add(btnExportarPdf);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(lblResumenSeleccionados, BorderLayout.NORTH);
        bottom.add(formatoPanel, BorderLayout.CENTER);
        bottom.add(botones, BorderLayout.SOUTH);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> {
            paginaActual = 1;
            cargarPagina(paginaActual);
        });

        btnLimpiarBusqueda.addActionListener(e -> {
            txtBusqueda.setText("");
            paginaActual = 1;
            cargarPagina(paginaActual);
        });

        txtBusqueda.addActionListener(e -> {
            paginaActual = 1;
            cargarPagina(paginaActual);
        });

        txtBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    paginaActual = 1;
                    cargarPagina(paginaActual);
                }
            }
        });

        btnAnterior.addActionListener(e -> {
            if (paginaActual > 1) {
                cargarPagina(paginaActual - 1);
            }
        });

        btnSiguiente.addActionListener(e -> {
            if (paginaActual < totalPaginas) {
                cargarPagina(paginaActual + 1);
            }
        });

        btnAgregarSeleccionado.addActionListener(e -> agregarSeleccionado());

        btnQuitarSeleccionado.addActionListener(e -> quitarSeleccionado());

        btnLimpiarLista.addActionListener(e -> limpiarLista());

        btnExportarPdf.addActionListener(e -> exportarPdf());

        tblPadron.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    agregarSeleccionado();
                }
            }
        });
    }

    private void cargarPagina(int paginaDeseada) {
        String criterio = txtBusqueda.getText().trim();

        setUIConsultando(true);
        setEstadoConsultando();

        SwingWorker<PadronPageData, Void> worker = new SwingWorker<PadronPageData, Void>() {
            @Override
            protected PadronPageData doInBackground() throws Exception {
                String json = client.explorar(criterio, paginaDeseada, TAMANO_PAGINA, "json");
                return JsonGuiParser.parsePadronPage(json);
            }

            @Override
            protected void done() {
                try {
                    PadronPageData data = get();

                    if (!data.isOk()) {
                        padronTableModel.setFilas(new ArrayList<>());
                        totalPaginas = 1;
                        paginaActual = 1;
                        lblResumenPadron.setText("Mostrando 0-0 de 0 registros | Página 1 de 1");
                        setEstadoError("error: " + data.getErrorMensaje());
                        JOptionPane.showMessageDialog(
                                PadronGuiFrame.this,
                                data.getErrorMensaje(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    criterioActual = data.getCriterio();
                    paginaActual = data.getPaginaActual();
                    totalPaginas = data.getTotalPaginas();

                    padronTableModel.setFilas(data.getResultados());
                    actualizarResumenPadron(data);

                    setEstadoListo("listo: página cargada");

                } catch (Exception ex) {
                    padronTableModel.setFilas(new ArrayList<>());
                    totalPaginas = 1;
                    paginaActual = 1;
                    lblResumenPadron.setText("Mostrando 0-0 de 0 registros | Página 1 de 1");
                    setEstadoError("error: backend no responde");
                    JOptionPane.showMessageDialog(
                            PadronGuiFrame.this,
                            construirMensajeError(ex),
                            "Error de conexión",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setUIConsultando(false);
                }
            }
        };

        worker.execute();
    }

    private void actualizarResumenPadron(PadronPageData data) {
        int total = data.getTotalResultados();
        int desde = total == 0 ? 0 : ((data.getPaginaActual() - 1) * data.getTamanoPagina()) + 1;
        int hasta = total == 0 ? 0 : Math.min(data.getPaginaActual() * data.getTamanoPagina(), total);

        lblResumenPadron.setText(
                "Mostrando " + desde + "-" + hasta + " de " + total
                + " registros | Página " + data.getPaginaActual()
                + " de " + data.getTotalPaginas()
        );

        btnAnterior.setEnabled(data.getPaginaActual() > 1);
        btnSiguiente.setEnabled(data.getPaginaActual() < data.getTotalPaginas());
    }

    private void agregarSeleccionado() {
        int viewRow = tblPadron.getSelectedRow();

        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debes seleccionar una persona del padrón.",
                    "Agregar seleccionado",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int modelRow = tblPadron.convertRowIndexToModel(viewRow);
        PadronRow fila = padronTableModel.getFila(modelRow);

        if (fila == null) {
            return;
        }

        if (seleccionadosMap.containsKey(fila.getCedula())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Esa persona ya está agregada en la lista.",
                    "Registro duplicado",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        setEstadoConsultando();

        SwingWorker<ConsultaSeleccionada, Void> worker = new SwingWorker<ConsultaSeleccionada, Void>() {
            @Override
            protected ConsultaSeleccionada doInBackground() throws Exception {
                String rawJson = client.consultarPorCedula(fila.getCedula(), "json");
                String rawXml = client.consultarPorCedula(fila.getCedula(), "xml");
                return JsonGuiParser.parseConsultaSeleccionada(rawJson, rawJson, rawXml);
            }

            @Override
            protected void done() {
                try {
                    ConsultaSeleccionada item = get();
                    seleccionadosMap.put(item.getCedula(), item);
                    refrescarSeleccionados();
                    setEstadoListo("listo: persona agregada");
                } catch (Exception ex) {
                    setEstadoError("error: no se pudo agregar");
                    JOptionPane.showMessageDialog(
                            PadronGuiFrame.this,
                            construirMensajeError(ex),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void quitarSeleccionado() {
        int viewRow = tblSeleccionados.getSelectedRow();

        if (viewRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debes seleccionar una persona de la lista.",
                    "Quitar seleccionado",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int modelRow = tblSeleccionados.convertRowIndexToModel(viewRow);
        ConsultaSeleccionada fila = seleccionadosTableModel.getFila(modelRow);

        if (fila != null) {
            seleccionadosMap.remove(fila.getCedula());
            refrescarSeleccionados();
            setEstadoListo("listo: persona eliminada");
        }
    }

    private void limpiarLista() {
        if (seleccionadosMap.isEmpty()) {
            return;
        }

        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas limpiar toda la lista seleccionada?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmar == JOptionPane.YES_OPTION) {
            seleccionadosMap.clear();
            refrescarSeleccionados();
            setEstadoListo("listo: lista limpiada");
        }
    }

    private void refrescarSeleccionados() {
        seleccionadosTableModel.setFilas(new ArrayList<>(seleccionadosMap.values()));
        lblResumenSeleccionados.setText("Seleccionados: " + seleccionadosMap.size());
    }

    private void exportarPdf() {
        if (seleccionadosMap.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay personas seleccionadas para exportar.",
                    "Exportar PDF",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo PDF", "pdf"));
        chooser.setSelectedFile(new File("reporte-padron.pdf"));

        int opcion = chooser.showSaveDialog(this);
        if (opcion != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = chooser.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".pdf")) {
            destino = new File(destino.getAbsolutePath() + ".pdf");
        }

        String formatoDetalle = String.valueOf(cmbFormatoDetalle.getSelectedItem());

        try {
            pdfExportService.exportar(
                    destino,
                    new ArrayList<>(seleccionadosMap.values()),
                    criterioActual,
                    formatoDetalle
            );

            setEstadoListo("listo: PDF exportado");
            JOptionPane.showMessageDialog(
                    this,
                    "PDF generado correctamente en:\n" + destino.getAbsolutePath(),
                    "Exportación exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException | DocumentException ex) {
            setEstadoError("error: no se pudo exportar PDF");
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo generar el PDF.\n\nDetalle: " + ex.getMessage(),
                    "Error al exportar",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setUIConsultando(boolean consultando) {
        btnBuscar.setEnabled(!consultando);
        btnLimpiarBusqueda.setEnabled(!consultando);
        btnAnterior.setEnabled(!consultando && paginaActual > 1);
        btnSiguiente.setEnabled(!consultando && paginaActual < totalPaginas);
        btnAgregarSeleccionado.setEnabled(!consultando);
        txtBusqueda.setEnabled(!consultando);
    }

    private String construirMensajeError(Exception ex) {
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();

        if (cause instanceof IOException) {
            return "No se pudo conectar con el backend en http://localhost:8080.\n"
                    + "Verifica que el servidor esté encendido e inténtalo de nuevo.";
        }

        return "Ocurrió un error inesperado.\nDetalle técnico: " + cause.getMessage();
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