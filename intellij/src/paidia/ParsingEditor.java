package paidia;

import javax.swing.*;

public abstract class ParsingEditor implements Editor {
    @Override
    public void endEdit(String text) {
        JComponent parsedComponent = ComponentParser.parseComponent(text);
        endEdit(parsedComponent);
    }

    protected abstract void endEdit(JComponent parsedComponent);
}
