package padron.presentacion.gui;

import com.itextpdf.text.DocumentException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.table.DefaultTableCellRenderer;

public class PadronGuiFrame extends JFrame {

    // ── PALETA BANDERA COSTA RICA ──────────────────────────────────────────
    private static final Color CR_AZUL_OSCURO  = new Color(0,   61,  165); // #003DA5
    private static final Color CR_AZUL_MEDIO   = new Color(21,  101, 192); // #1565C0
    private static final Color CR_AZUL_CLARO   = new Color(227, 234, 246); // #E3EAF6
    private static final Color CR_AZUL_FONDO   = new Color(240, 244, 250); // #F0F4FA
    private static final Color CR_ROJO_OSCURO  = new Color(200, 16,  46);  // #C8102E
    private static final Color CR_ROJO_VIVO    = new Color(229, 57,  53);  // #E53935
    private static final Color CR_BLANCO       = Color.WHITE;
    // ──────────────────────────────────────────────────────────────────────

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

        estilizarBotones();
        construirUI();
        configurarEventos();

        pack();
        setSize(1260, 760);
        setLocationRelativeTo(null);

        cargarPagina(1);
    }

    // ── Estilos de botones ─────────────────────────────────────────────────

    private void estilizarBotones() {
        // Botón primario: rojo CR (acción de exportar)
        estilizarBotonPrimario(btnExportarPdf);

        // Botones secundarios: azul CR
        estilizarBotonSecundario(btnBuscar);
        estilizarBotonSecundario(btnAgregarSeleccionado);

        // Botones neutros: borde azul medio, texto azul oscuro
        estilizarBotonNeutro(btnLimpiarBusqueda);
        estilizarBotonNeutro(btnAnterior);
        estilizarBotonNeutro(btnSiguiente);
        estilizarBotonNeutro(btnQuitarSeleccionado);
        estilizarBotonNeutro(btnLimpiarLista);
    }

    private void estilizarBotonPrimario(JButton btn) {
        btn.setBackground(CR_ROJO_OSCURO);
        btn.setForeground(CR_BLANCO);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
    }

    private void estilizarBotonSecundario(JButton btn) {
        btn.setBackground(CR_AZUL_OSCURO);
        btn.setForeground(CR_BLANCO);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 13f));
    }

    private void estilizarBotonNeutro(JButton btn) {
        btn.setBackground(CR_BLANCO);
        btn.setForeground(CR_AZUL_OSCURO);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(CR_AZUL_MEDIO));
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 13f));
    }

    // ── Construcción de UI ─────────────────────────────────────────────────

    private void construirUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(CR_AZUL_FONDO);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                construirPanelIzquierdo(),
                construirPanelDerecho()
        );
        splitPane.setResizeWeight(0.68);

        JPanel panelEstado = new JPanel(new BorderLayout());
        panelEstado.setBackground(CR_AZUL_FONDO);
        panelEstado.setBorder(new EmptyBorder(4, 4, 0, 4));

        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 13f));
        lblEstado.setForeground(CR_AZUL_OSCURO);

        panelEstado.add(lblEstado, BorderLayout.WEST);

        root.add(construirBandera(), BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);
        root.add(panelEstado, BorderLayout.SOUTH);

        setContentPane(root);
    }

    /**
     * Barra decorativa con las franjas de la bandera de Costa Rica.
     * Proporciones oficiales: azul(1) - blanco(1) - rojo(2) - blanco(1) - azul(1)
     * Altura total: 18px, se ve como una franja fina pero reconocible.
     */
    private JPanel construirBandera() {
        // Pesos de cada franja según proporciones reales de la bandera CR
        int[] pesos = {1, 1, 2, 1, 1};
        Color[] colores = {CR_AZUL_OSCURO, CR_BLANCO, CR_ROJO_OSCURO, CR_BLANCO, CR_AZUL_OSCURO};
        int total = 6; // suma de pesos

        JPanel bandera = new JPanel(null); // layout absoluto para control exacto de proporciones
        bandera.setPreferredSize(new Dimension(0, 18));

        // Usamos un componente que recalcula al redimensionar
        JPanel franjas = new JPanel() {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                int x = 0;
                Component[] comps = getComponents();
                for (int i = 0; i < comps.length; i++) {
                    int franjaH = (h * pesos[i]) / total;
                    // última franja toma el resto para evitar píxeles sobrantes
                    if (i == comps.length - 1) {
                        franjaH = h - x; // x acumula altura usada
                    }
                    comps[i].setBounds(0, x, w, franjaH);
                    x += franjaH;
                }
            }
        };
        franjas.setLayout(null);
        franjas.setPreferredSize(new Dimension(0, 18));

        for (Color color : colores) {
            JPanel franja = new JPanel();
            franja.setBackground(color);
            franja.setOpaque(true);
            franjas.add(franja);
        }

        return franjas;
    }

    private JPanel construirPanelIzquierdo() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CR_BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CR_AZUL_MEDIO),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titulo = new JLabel("Padrón / Búsqueda");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setForeground(CR_AZUL_OSCURO);

        JLabel ayuda = new JLabel("Deja el campo vacío para explorar todo el padrón o escribe nombre/cédula para filtrar.");
        ayuda.setForeground(CR_AZUL_MEDIO);

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);
        topInfo.add(titulo, BorderLayout.NORTH);
        topInfo.add(ayuda, BorderLayout.SOUTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelBusqueda.setOpaque(false);
        JLabel lblNombreCedula = new JLabel("Nombre o cédula:");
        lblNombreCedula.setForeground(CR_AZUL_OSCURO);
        panelBusqueda.add(lblNombreCedula);
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnLimpiarBusqueda);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(topInfo, BorderLayout.NORTH);
        top.add(panelBusqueda, BorderLayout.SOUTH);

        // Tabla con estilo CR
        tblPadron.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPadron.setRowHeight(24);
        tblPadron.setAutoCreateRowSorter(true);
        tblPadron.setSelectionBackground(CR_ROJO_OSCURO);
        tblPadron.setSelectionForeground(CR_BLANCO);
        tblPadron.setDefaultRenderer(Object.class, new FilasAlternasRenderer());

        // Header azul oscuro
        tblPadron.getTableHeader().setBackground(CR_AZUL_OSCURO);
        tblPadron.getTableHeader().setForeground(CR_AZUL_OSCURO);
        tblPadron.getTableHeader().setFont(
                tblPadron.getTableHeader().getFont().deriveFont(Font.BOLD, 13f)
        );

        JScrollPane scroll = new JScrollPane(tblPadron);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CR_AZUL_MEDIO),
                "Resultados"
        ));

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);

        lblResumenPadron.setForeground(CR_AZUL_MEDIO);

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
        panel.setBackground(CR_BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CR_AZUL_MEDIO),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titulo = new JLabel("Lista seleccionada para PDF");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        titulo.setForeground(CR_AZUL_OSCURO);

        // Tabla con estilo CR
        tblSeleccionados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSeleccionados.setRowHeight(24);
        tblSeleccionados.setAutoCreateRowSorter(true);
        tblSeleccionados.setSelectionBackground(CR_ROJO_OSCURO);
        tblSeleccionados.setSelectionForeground(CR_BLANCO);
        tblSeleccionados.setDefaultRenderer(Object.class, new FilasAlternasRenderer());

        // Header azul oscuro
        tblSeleccionados.getTableHeader().setBackground(CR_AZUL_OSCURO);
        tblSeleccionados.getTableHeader().setForeground(CR_AZUL_OSCURO);
        tblSeleccionados.getTableHeader().setFont(
                tblSeleccionados.getTableHeader().getFont().deriveFont(Font.BOLD, 13f)
        );

        JScrollPane scroll = new JScrollPane(tblSeleccionados);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CR_AZUL_MEDIO),
                "Seleccionados"
        ));

        JPanel formatoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        formatoPanel.setOpaque(false);
        JLabel lblFormato = new JLabel("Formato detalle PDF:");
        lblFormato.setForeground(CR_AZUL_OSCURO);
        formatoPanel.add(lblFormato);
        formatoPanel.add(cmbFormatoDetalle);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnQuitarSeleccionado);
        botones.add(btnLimpiarLista);
        botones.add(btnExportarPdf);

        lblResumenSeleccionados.setForeground(CR_AZUL_MEDIO);

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



    // ── Eventos ────────────────────────────────────────────────────────────

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

    // ── Lógica de negocio (sin cambios) ───────────────────────────────────

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

    // ── Estado con colores CR ──────────────────────────────────────────────

    private void setEstadoListo(String detalle) {
        lblEstado.setForeground(CR_AZUL_OSCURO);
        lblEstado.setText("Estado: " + detalle);
    }

    private void setEstadoConsultando() {
        lblEstado.setForeground(CR_ROJO_VIVO);
        lblEstado.setText("Estado: consultando...");
    }

    private void setEstadoError(String detalle) {
        lblEstado.setForeground(CR_ROJO_OSCURO);
        lblEstado.setText("Estado: " + detalle);
    }

    // ── Renderer filas alternas ────────────────────────────────────────────

    private static class FilasAlternasRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setOpaque(true);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(227, 234, 246));
                setForeground(Color.BLACK);
            }
            return this;
        }
    }
}