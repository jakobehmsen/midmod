package paidia;

import javax.swing.*;

public interface ViewBinding {
    JComponent getView();
    void release();
    boolean isCompatibleWith(Value value);
    void updateFrom(Value value);
    default void setupEditor(ConstructorCell editor) {

    }
    default void setupWorkspace(Workspace workspace) {

    }
}
