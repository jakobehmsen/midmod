package midmod.editor;

import midmod.MapCell;

import javax.swing.*;
import java.awt.*;

public class MapCellEditor extends JPanel {
    private MapCell mapCell;

    public MapCellEditor(MapCell mapCell) {
        this.mapCell = mapCell;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JLabel l1 = new JLabel("{");
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        l1.setAlignmentY(Component.TOP_ALIGNMENT);
        add(l1);
        add(new ProxyCellEditor());
        JLabel l2 = new JLabel("}");
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        l2.setAlignmentY(Component.TOP_ALIGNMENT);
        add(l2);
    }
}
