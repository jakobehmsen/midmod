package paidia;

import javax.swing.*;

public interface ViewBinding {
    JComponent getView();
    void release();
}
