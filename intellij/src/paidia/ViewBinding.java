package paidia;

import javax.swing.*;

public interface ViewBinding {
    JComponent getView();
    void release();
    boolean isCompatibleWith(Value value);
    void updateFrom(Value value);
}
