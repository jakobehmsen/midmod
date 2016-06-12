package paidia;

import javax.swing.*;

public interface EditableViewListener {
    //void endEdit(JComponent newValueView);
    void endEdit(String text);
    void cancelEdit();
}
