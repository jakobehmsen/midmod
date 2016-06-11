package paidia;

import javax.swing.*;
import java.awt.*;

public interface ValueViewContainer {
    EditableView getEditorFor(JComponent valueView, Point location);
}
