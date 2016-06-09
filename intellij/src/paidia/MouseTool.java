package paidia;

import javax.swing.*;
import java.awt.event.MouseAdapter;

public abstract class MouseTool extends MouseAdapter {
    public void startTool(JComponent component) {

    }

    public void endTool(JComponent component) {
        component.setToolTipText("");
    }
}
