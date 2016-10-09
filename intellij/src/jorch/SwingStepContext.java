package jorch;

import javax.swing.*;

public interface SwingStepContext {
    JFrame getFrame();
    JComponent getComponent();
    void halt();
    void resume();
}
