package padron.presentacion.gui;

import com.itextpdf.text.DocumentException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import padron.logica.ServicioCorreo;

public class PadronGuiFrame extends JFrame {

    private static final Color CR_AZUL_OSCURO = new Color(0, 61, 165);
    private static final Color CR_AZUL_MEDIO = new Color(21, 101, 192);
    private static final Color CR_AZUL_CLARO = new Color(227, 234, 246);
    private static final Color CR_AZUL_FONDO = new Color(240, 244, 250);
    private static final Color CR_ROJO_OSCURO = new Color(200, 16, 46);
    private static final Color CR_ROJO_VIVO = new Color(229, 57, 53);
    private static final Color CR_BLANCO = Color.WHITE;

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
    private File ultimoPdfGenerado;
    private final JButton btnEnviarCorreo;
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
        btnEnviarCorreo = new JButton("Enviar correo");
        cmbFormatoDetalle = new JComboBox<>(new String[]{"JSON", "XML"});

        lblResumenPadron = new JLabel("Mostrando 0-0 de 0 registros | Página 1 de 1");
        lblResumenSeleccionados = new JLabel("Seleccionados: 0");
        lblEstado = new JLabel("Estado: listo");

        configurarIconosVentana();
        estilizarBotones();
        construirUI();
        configurarEventos();

        pack();
        setSize(1260, 760);
        setLocationRelativeTo(null);

        cargarPagina(1);
    }

    private void configurarIconosVentana() {
        List<Image> iconos = new ArrayList<>();

        agregarIcono(iconos, "/padron/presentacion/gui/resources/Logo16.png");
        agregarIcono(iconos, "/padron/presentacion/gui/resources/Logo32.png");
        agregarIcono(iconos, "/padron/presentacion/gui/resources/Logo64.png");
        agregarIcono(iconos, "/padron/presentacion/gui/resources/Logo128.png");
        agregarIcono(iconos, "/padron/presentacion/gui/resources/Logo256.png");

        if (!iconos.isEmpty()) {
            setIconImages(iconos);
            setIconImage(iconos.get(iconos.size() - 1));
        }
    }

    private void agregarIcono(List<Image> iconos, String ruta) {
        URL url = getClass().getResource(ruta);
        if (url != null) {
            iconos.add(new ImageIcon(url).getImage());
        }
    }

    private Image getBestAppIcon() {
        URL url = getClass().getResource("/padron/presentacion/gui/resources/Logo128.png");
        if (url != null) {
            return new ImageIcon(url).getImage();
        }

        url = getClass().getResource("/padron/presentacion/gui/resources/Logo64.png");
        if (url != null) {
            return new ImageIcon(url).getImage();
        }

        url = getClass().getResource("/padron/presentacion/gui/resources/Logo32.png");
        if (url != null) {
            return new ImageIcon(url).getImage();
        }

        return null;
    }

    private void estilizarBotones() {
        estilizarBotonPrimario(btnExportarPdf);

        estilizarBotonSecundario(btnBuscar);
        estilizarBotonSecundario(btnAgregarSeleccionado);
        estilizarBotonSecundario(btnEnviarCorreo);

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

    private JPanel construirBandera() {
        int[] pesos = {1, 1, 2, 1, 1};
        Color[] colores = {
            CR_AZUL_OSCURO,
            CR_BLANCO,
            CR_ROJO_OSCURO,
            CR_BLANCO,
            CR_AZUL_OSCURO
        };
        int total = 6;

        JPanel bandera = new JPanel(null);
        bandera.setPreferredSize(new Dimension(0, 18));

        JPanel franjas = new JPanel() {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                int y = 0;
                Component[] comps = getComponents();

                for (int i = 0; i < comps.length; i++) {
                    int franjaH = (h * pesos[i]) / total;
                    if (i == comps.length - 1) {
                        franjaH = h - y;
                    }
                    comps[i].setBounds(0, y, w, franjaH);
                    y += franjaH;
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

        tblPadron.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPadron.setRowHeight(24);
        tblPadron.setAutoCreateRowSorter(true);
        tblPadron.setSelectionBackground(CR_ROJO_OSCURO);
        tblPadron.setSelectionForeground(CR_BLANCO);
        tblPadron.setDefaultRenderer(Object.class, new FilasAlternasRenderer());
        estilizarHeaderTabla(tblPadron);

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

        tblSeleccionados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSeleccionados.setRowHeight(24);
        tblSeleccionados.setAutoCreateRowSorter(true);
        tblSeleccionados.setSelectionBackground(CR_ROJO_OSCURO);
        tblSeleccionados.setSelectionForeground(CR_BLANCO);
        tblSeleccionados.setDefaultRenderer(Object.class, new FilasAlternasRenderer());
        estilizarHeaderTabla(tblSeleccionados);

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
        botones.add(btnEnviarCorreo);
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

    private void estilizarHeaderTabla(JTable tabla) {
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setOpaque(true);
        headerRenderer.setBackground(CR_AZUL_CLARO);
        headerRenderer.setForeground(Color.BLACK);
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        headerRenderer.setBorder(BorderFactory.createLineBorder(CR_AZUL_OSCURO));

        tabla.getTableHeader().setDefaultRenderer(headerRenderer);
        tabla.getTableHeader().setReorderingAllowed(false);
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
        btnEnviarCorreo.addActionListener(e -> enviarCorreo());

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
                    setEstadoError("error: consulta fallida");

                    JOptionPane.showMessageDialog(
                            PadronGuiFrame.this,
                            construirMensajeError(ex),
                            "Error de consulta",
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

            ultimoPdfGenerado = destino;
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

    private void enviarCorreo() {
        if (ultimoPdfGenerado == null || !ultimoPdfGenerado.exists()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero debes generar el PDF antes de enviarlo por correo.",
                    "Enviar correo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        FormularioCorreoData datos = mostrarDialogoCorreo(this);
        if (datos == null) {
            return;
        }

        try {
            ServicioCorreo servicio = new ServicioCorreo(
                    "sandijuan21@gmail.com",
                    "kdfovuyurccmeynh"
            );

            servicio.enviarConAdjunto(
                    datos.destino,
                    datos.cc,
                    datos.cco,
                    "Reporte del Padrón",
                    "Adjunto encontrarás el PDF generado.",
                    ultimoPdfGenerado
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Correo enviado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al enviar correo:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private FormularioCorreoData mostrarDialogoCorreo(Window parent) {
        JDialog dialog = new JDialog(this, "Enviar correo", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        Image icono = getBestAppIcon();
        if (icono != null) {
            dialog.setIconImage(icono);
        }

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        root.setBackground(CR_AZUL_FONDO);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblPara = new JLabel("Para:");
        JLabel lblCc = new JLabel("CC:");
        JLabel lblCco = new JLabel("CCO:");

        lblPara.setForeground(CR_AZUL_OSCURO);
        lblCc.setForeground(CR_AZUL_OSCURO);
        lblCco.setForeground(CR_AZUL_OSCURO);

        JTextField txtPara = new JTextField(28);
        JTextField txtCc = new JTextField(28);
        JTextField txtCco = new JTextField(28);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(lblPara, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(txtPara, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(lblCc, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(txtCc, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        form.add(lblCco, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(txtCco, gbc);

        JLabel ayuda = new JLabel("Puedes escribir varios correos en CC y CCO separados por coma.");
        ayuda.setForeground(CR_AZUL_MEDIO);

        JButton btnCancelar = new JButton("Cancelar");
        JButton btnEnviar = new JButton("Enviar");

        estilizarBotonNeutro(btnCancelar);
        estilizarBotonSecundario(btnEnviar);

        final FormularioCorreoData[] resultado = new FormularioCorreoData[1];

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnEnviar.addActionListener(e -> {
            String para = txtPara.getText().trim();
            String cc = txtCc.getText().trim();
            String cco = txtCco.getText().trim();

            if (para.isEmpty()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "El correo principal es obligatorio.",
                        "Dato requerido",
                        JOptionPane.WARNING_MESSAGE
                );
                txtPara.requestFocusInWindow();
                return;
            }

            resultado[0] = new FormularioCorreoData(para, cc, cco);
            dialog.dispose();
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnEnviar);

        root.add(form, BorderLayout.NORTH);
        root.add(ayuda, BorderLayout.CENTER);
        root.add(botones, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return resultado[0];
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
        String detalle = cause.getMessage() == null ? "(sin detalle)" : cause.getMessage();

        if (cause instanceof IOException) {
            return "Falló la consulta HTTP al backend.\n"
                    + "El servidor podría estar apagado, o la URL/parámetros de búsqueda podrían ser inválidos.\n\n"
                    + "Detalle técnico: " + detalle;
        }

        return "Ocurrió un error inesperado.\nDetalle técnico: " + detalle;
    }

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

    private static class FormularioCorreoData {
        private final String destino;
        private final String cc;
        private final String cco;

        private FormularioCorreoData(String destino, String cc, String cco) {
            this.destino = destino;
            this.cc = cc;
            this.cco = cco;
        }
    }
}