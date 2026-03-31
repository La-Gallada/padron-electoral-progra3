package padron.presentacion.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class SeleccionadosTableModel extends AbstractTableModel {

    private final String[] columnas = {
        "Cédula", "Nombre completo", "Provincia", "Cantón", "Distrito"
    };

    private List<ConsultaSeleccionada> filas = new ArrayList<>();

    public void setFilas(List<ConsultaSeleccionada> filas) {
        this.filas = filas == null ? new ArrayList<>() : new ArrayList<>(filas);
        fireTableDataChanged();
    }

    public ConsultaSeleccionada getFila(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= filas.size()) {
            return null;
        }
        return filas.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ConsultaSeleccionada fila = filas.get(rowIndex);

        switch (columnIndex) {
            case 0: return fila.getCedula();
            case 1: return fila.getNombreCompleto();
            case 2: return fila.getProvincia();
            case 3: return fila.getCanton();
            case 4: return fila.getDistrito();
            default: return "";
        }
    }
}