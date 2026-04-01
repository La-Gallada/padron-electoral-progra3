package padron.presentacion.gui;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {

    // ── PALETA BANDERA COSTA RICA ──────────────────────────────────────────
    private static final BaseColor CR_AZUL_OSCURO = new BaseColor(0,   61,  165);
    private static final BaseColor CR_AZUL_MEDIO  = new BaseColor(21,  101, 192);
    private static final BaseColor CR_AZUL_CLARO  = new BaseColor(227, 234, 246);
    private static final BaseColor CR_ROJO_OSCURO = new BaseColor(200, 16,  46);
    private static final BaseColor CR_BLANCO      = BaseColor.WHITE;
    private static final BaseColor CR_GRIS_TEXTO  = new BaseColor(50,  50,  50);
    // ──────────────────────────────────────────────────────────────────────

    public void exportar(File destino,
                         List<ConsultaSeleccionada> seleccionados,
                         String criterio,
                         String formatoDetalle) throws IOException, DocumentException {

        if (destino == null) {
            throw new IOException("No se seleccionó archivo destino.");
        }

        if (seleccionados == null || seleccionados.isEmpty()) {
            throw new IOException("No hay personas seleccionadas para exportar.");
        }

        String formato = formatoDetalle == null ? "JSON" : formatoDetalle.trim().toUpperCase();

        File parent = destino.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destino));

        // Evento para dibujar la bandera en cada página
        writer.setPageEvent(new BanderaPageEvent());

        document.open();

        // ── Fuentes con paleta CR ──────────────────────────────────────────
        Font titleFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, CR_AZUL_OSCURO);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA,      10, CR_GRIS_TEXTO);
        Font labelFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, CR_AZUL_MEDIO);
        Font headerFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, CR_BLANCO);
        Font cellFont     = FontFactory.getFont(FontFactory.HELVETICA,       9, CR_GRIS_TEXTO);
        Font sectionFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, CR_AZUL_OSCURO);
        Font rawFont      = FontFactory.getFont(FontFactory.COURIER,         8, CR_GRIS_TEXTO);

        // ── Espacio para que el contenido no tape la bandera superior ──────
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // ── Título ─────────────────────────────────────────────────────────
        Paragraph titulo = new Paragraph("Reporte de selección del padrón electoral", titleFont);
        titulo.setSpacingAfter(4f);
        document.add(titulo);

        // Línea decorativa azul bajo el título (simulada con tabla de 1 fila)
        PdfPTable lineaDecorativa = new PdfPTable(1);
        lineaDecorativa.setWidthPercentage(100);
        PdfPCell lineaCell = new PdfPCell(new Phrase(""));
        lineaCell.setBackgroundColor(CR_ROJO_OSCURO);
        lineaCell.setFixedHeight(3f);
        lineaCell.setBorder(Rectangle.NO_BORDER);
        lineaDecorativa.addCell(lineaCell);
        lineaDecorativa.setSpacingAfter(10f);
        document.add(lineaDecorativa);

        // ── Metadatos del reporte ──────────────────────────────────────────
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        PdfPTable metaTable = new PdfPTable(new float[]{1.5f, 4f});
        metaTable.setWidthPercentage(60);
        metaTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
        metaTable.setSpacingAfter(14f);

        agregarMeta(metaTable, "Fecha de exportación:", fecha,              labelFont, subtitleFont);
        agregarMeta(metaTable, "Criterio de búsqueda:",
                (criterio == null || criterio.isBlank() ? "Sin filtro" : criterio), labelFont, subtitleFont);
        agregarMeta(metaTable, "Total exportados:",     String.valueOf(seleccionados.size()), labelFont, subtitleFont);
        agregarMeta(metaTable, "Formato de detalle:",   formato,            labelFont, subtitleFont);

        document.add(metaTable);

        // ── Tabla principal ────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(new float[]{2.2f, 2.3f, 2.0f, 2.0f, 1.7f, 2.2f, 2.0f, 2.0f, 2.0f});
        table.setWidthPercentage(100);
        table.setSpacingAfter(14f);

        agregarHeader(table, "Cédula",       headerFont);
        agregarHeader(table, "Nombre",       headerFont);
        agregarHeader(table, "1er apellido", headerFont);
        agregarHeader(table, "2do apellido", headerFont);
        agregarHeader(table, "CodElec",      headerFont);
        agregarHeader(table, "Provincia",    headerFont);
        agregarHeader(table, "Cantón",       headerFont);
        agregarHeader(table, "Distrito",     headerFont);
        agregarHeader(table, "Recinto",      headerFont);

        boolean filaAlterna = false;
        for (ConsultaSeleccionada item : seleccionados) {
            BaseColor bgFila = filaAlterna ? CR_AZUL_CLARO : CR_BLANCO;
            agregarCell(table, item.getCedula(),          cellFont, bgFila);
            agregarCell(table, item.getNombre(),          cellFont, bgFila);
            agregarCell(table, item.getPrimerApellido(),  cellFont, bgFila);
            agregarCell(table, item.getSegundoApellido(), cellFont, bgFila);
            agregarCell(table, item.getCodElec(),         cellFont, bgFila);
            agregarCell(table, item.getProvincia(),       cellFont, bgFila);
            agregarCell(table, item.getCanton(),          cellFont, bgFila);
            agregarCell(table, item.getDistrito(),        cellFont, bgFila);
            agregarCell(table, item.getRecinto(),         cellFont, bgFila);
            filaAlterna = !filaAlterna;
        }

        document.add(table);

        // ── Sección de detalle ─────────────────────────────────────────────
        Paragraph seccion = new Paragraph("Detalle en formato " + formato, sectionFont);
        seccion.setSpacingAfter(6f);
        document.add(seccion);

        // Línea roja bajo el subtítulo de sección
        PdfPTable lineaSeccion = new PdfPTable(1);
        lineaSeccion.setWidthPercentage(100);
        PdfPCell lsCell = new PdfPCell(new Phrase(""));
        lsCell.setBackgroundColor(CR_AZUL_MEDIO);
        lsCell.setFixedHeight(2f);
        lsCell.setBorder(Rectangle.NO_BORDER);
        lineaSeccion.addCell(lsCell);
        lineaSeccion.setSpacingAfter(10f);
        document.add(lineaSeccion);

        int indice = 1;
        for (ConsultaSeleccionada item : seleccionados) {
            Paragraph encabezadoPersona = new Paragraph(
                    indice + ". " + item.getNombreCompleto() + " (" + item.getCedula() + ")",
                    sectionFont
            );
            encabezadoPersona.setSpacingBefore(6f);
            encabezadoPersona.setSpacingAfter(4f);
            document.add(encabezadoPersona);

            String bloque = "XML".equals(formato) ? item.getRawXml() : item.getRawJson();
            if (bloque == null || bloque.isBlank()) {
                bloque = "[Sin detalle disponible]";
            }

            // Bloque de código con fondo azul claro
            PdfPTable bloqueTable = new PdfPTable(1);
            bloqueTable.setWidthPercentage(100);
            bloqueTable.setSpacingAfter(8f);
            PdfPCell bloqueCell = new PdfPCell(new Phrase(bloque, rawFont));
            bloqueCell.setBackgroundColor(CR_AZUL_CLARO);
            bloqueCell.setPadding(8f);
            bloqueCell.setBorderColor(CR_AZUL_MEDIO);
            bloqueTable.addCell(bloqueCell);
            document.add(bloqueTable);

            indice++;
        }

        document.close();
    }

    // ── Helpers de tabla ──────────────────────────────────────────────────

    private void agregarHeader(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(CR_AZUL_OSCURO);
        cell.setPadding(6f);
        cell.setBorderColor(CR_AZUL_MEDIO);
        table.addCell(cell);
    }

    private void agregarCell(PdfPTable table, String texto, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "" : texto, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5f);
        cell.setBorderColor(CR_AZUL_CLARO);
        table.addCell(cell);
    }

    private void agregarMeta(PdfPTable table, String label, String valor, Font fontLabel, Font fontValor) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fontLabel));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setPaddingBottom(4f);
        table.addCell(cLabel);

        PdfPCell cValor = new PdfPCell(new Phrase(valor, fontValor));
        cValor.setBorder(Rectangle.NO_BORDER);
        cValor.setPaddingBottom(4f);
        table.addCell(cValor);
    }

    // ── Evento de página: bandera CR en header y footer ───────────────────

    private static class BanderaPageEvent extends PdfPageEventHelper {

        // Proporciones reales: azul(1) blanco(1) rojo(2) blanco(1) azul(1) → total 6
        private static final BaseColor CR_AZUL  = new BaseColor(0,   61,  165);
        private static final BaseColor CR_ROJO  = new BaseColor(200, 16,  46);
        private static final BaseColor CR_BLANC = BaseColor.WHITE;

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle pageSize = document.getPageSize();

            float pageWidth = pageSize.getWidth();
            float top       = pageSize.getTop();
            float bottom    = pageSize.getBottom();

            // ── Bandera en la parte superior (altura total 12pt) ──────────
            float alturaTotal = 12f;
            float alturaUnidad = alturaTotal / 6f;

            dibujarFranja(cb, 0, top - alturaUnidad * 1, pageWidth, alturaUnidad * 1, CR_AZUL);  // azul
            dibujarFranja(cb, 0, top - alturaUnidad * 2, pageWidth, alturaUnidad * 1, CR_BLANC); // blanco
            dibujarFranja(cb, 0, top - alturaUnidad * 4, pageWidth, alturaUnidad * 2, CR_ROJO);  // rojo (doble)
            dibujarFranja(cb, 0, top - alturaUnidad * 5, pageWidth, alturaUnidad * 1, CR_BLANC); // blanco
            dibujarFranja(cb, 0, top - alturaUnidad * 6, pageWidth, alturaUnidad * 1, CR_AZUL);  // azul

            // ── Línea delgada roja en el footer ───────────────────────────
            dibujarFranja(cb, 0, bottom, pageWidth, 4f, CR_ROJO);
        }

        private void dibujarFranja(PdfContentByte cb, float x, float y,
                                   float ancho, float alto, BaseColor color) {
            cb.saveState();
            cb.setColorFill(color);
            cb.rectangle(x, y, ancho, alto);
            cb.fill();
            cb.restoreState();
        }
    }
}