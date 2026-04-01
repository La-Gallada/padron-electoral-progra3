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
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportService {

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
        PdfWriter.getInstance(document, new FileOutputStream(destino));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font rawFont = FontFactory.getFont(FontFactory.COURIER, 8, BaseColor.BLACK);

        document.add(new Paragraph("Reporte de selección del padrón electoral", titleFont));
        document.add(Chunk.NEWLINE);

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        document.add(new Paragraph("Fecha de exportación: " + fecha, subtitleFont));
        document.add(new Paragraph("Criterio de búsqueda: " + (criterio == null || criterio.isBlank() ? "Sin filtro" : criterio), subtitleFont));
        document.add(new Paragraph("Total de personas exportadas: " + seleccionados.size(), subtitleFont));
        document.add(new Paragraph("Formato de detalle: " + formato, subtitleFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(new float[]{2.2f, 2.3f, 2.0f, 2.0f, 1.7f, 2.2f, 2.0f, 2.0f, 2.0f});
        table.setWidthPercentage(100);

        agregarHeader(table, "Cédula", headerFont);
        agregarHeader(table, "Nombre", headerFont);
        agregarHeader(table, "1er apellido", headerFont);
        agregarHeader(table, "2do apellido", headerFont);
        agregarHeader(table, "CodElec", headerFont);
        agregarHeader(table, "Provincia", headerFont);
        agregarHeader(table, "Cantón", headerFont);
        agregarHeader(table, "Distrito", headerFont);
        agregarHeader(table, "Recinto", headerFont);

        for (ConsultaSeleccionada item : seleccionados) {
            agregarCell(table, item.getCedula(), cellFont);
            agregarCell(table, item.getNombre(), cellFont);
            agregarCell(table, item.getPrimerApellido(), cellFont);
            agregarCell(table, item.getSegundoApellido(), cellFont);
            agregarCell(table, item.getCodElec(), cellFont);
            agregarCell(table, item.getProvincia(), cellFont);
            agregarCell(table, item.getCanton(), cellFont);
            agregarCell(table, item.getDistrito(), cellFont);
            agregarCell(table, item.getRecinto(), cellFont);
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Detalle en formato " + formato, sectionFont));
        document.add(Chunk.NEWLINE);

        int indice = 1;
        for (ConsultaSeleccionada item : seleccionados) {
            document.add(new Paragraph(indice + ". " + item.getNombreCompleto() + " (" + item.getCedula() + ")", sectionFont));

            String bloque = "XML".equals(formato) ? item.getRawXml() : item.getRawJson();
            if (bloque == null || bloque.isBlank()) {
                bloque = "[Sin detalle disponible]";
            }

            document.add(new Paragraph(bloque, rawFont));
            document.add(Chunk.NEWLINE);
            indice++;
        }

        document.close();
    }

    private void agregarHeader(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(new BaseColor(33, 150, 243));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void agregarCell(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "" : texto, font));
        cell.setPadding(5f);
        table.addCell(cell);
    }
}