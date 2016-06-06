package paidia;

import javax.swing.*;

public interface Editor {
    String getText();
    void beginEdit(JComponent editorComponent);
    void endEdit(String text);
    void cancelEdit();
}
