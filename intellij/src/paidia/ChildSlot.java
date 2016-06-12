package paidia;

import javax.swing.*;

public interface ChildSlot {
    void replace(JComponent view);
    void revert();
    void commit(JComponent valueView);
}
