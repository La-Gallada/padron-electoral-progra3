package padron.presentacion.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class PadronTableModel extends AbstractTableModel {

    private final String[] columnas = {
        "Cédula", "Nombre", "Primer apellido", "Segundo apellido", "CodElec"
    };

    private List<PadronRow> filas = new ArrayList<>();

    public void setFilas(List<PadronRow> filas) {
        this.filas = filas == null ? new ArrayList<>() : new ArrayList<>(filas);
        fireTableDataChanged();
    }

    public PadronRow getFila(int rowIndex) {
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
        PadronRow fila = filas.get(rowIndex);

        switch (columnIndex) {
            case 0: return fila.getCedula();
            case 1: return fila.getNombre();
            case 2: return fila.getPrimerApellido();
            case 3: return fila.getSegundoApellido();
            case 4: return fila.getCodElec();
            default: return "";
        }
    }
}