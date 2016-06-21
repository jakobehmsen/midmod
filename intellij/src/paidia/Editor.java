package paidia;

import javax.swing.*;

public interface Editor {
    String getText();
    void beginEdit(JComponent editorComponent);
    //void endEdit(JComponent parsedComponent);
    void endEdit(String text);
    void cancelEdit();
}
