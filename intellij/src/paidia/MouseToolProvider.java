package paidia;

import javax.swing.*;

public interface MouseToolProvider {
    MouseTool getMouseTool();
    void setCanvas(JComponent canvas);
}
