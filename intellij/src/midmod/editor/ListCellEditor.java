package midmod.editor;

import midmod.ListCell;

import javax.swing.*;

public class ListCellEditor extends JPanel {
    private ListCell listCell;

    public ListCellEditor(ListCell listCell) {
        this.listCell = listCell;

        add(new JLabel("["));
        add(new ProxyCellEditor((current, replacement) -> {

        }));
        add(new JLabel("]"));
    }
}
