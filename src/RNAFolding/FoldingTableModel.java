package RNAFolding;

import javax.swing.table.DefaultTableModel;

/**
 * Small extension of DefaultTableModel to ensure proper sorting
 *
 * @author Eirik Krogstad
 */
public class FoldingTableModel extends DefaultTableModel {

    public FoldingTableModel(Object columnNames[], int rowCount) {
        super(columnNames, rowCount);
    }

    @Override
    public Class getColumnClass(int col) {
        if (columnIdentifiers.get(col).equals("Number") ||
            columnIdentifiers.get(col).equals("ID"))
            return Integer.class;
        else return String.class;
    }
}
