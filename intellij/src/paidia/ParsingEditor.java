package paidia;

import javax.swing.*;

public abstract class ParsingEditor implements Editor {
    private PlaygroundView playgroundView;

    public ParsingEditor(PlaygroundView playgroundView) {
        this.playgroundView = playgroundView;
    }

    @Override
    public void endEdit(String text) {
        JComponent parsedComponent = ComponentParser.parseComponent(text, playgroundView);
        endEdit(parsedComponent);
    }

    protected abstract void endEdit(JComponent parsedComponent);
}
