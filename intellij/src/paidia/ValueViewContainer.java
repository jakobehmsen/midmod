package paidia;

import javax.swing.*;

public interface ValueViewContainer {
    EditableView getEditorFor(JComponent valueView);
}
