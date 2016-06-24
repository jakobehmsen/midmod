package paidia;

import javax.swing.*;

public abstract class ParsingEditor implements Editor {
    private PlaygroundView playgroundView;

    public ParsingEditor(PlaygroundView playgroundView) {
        this.playgroundView = playgroundView;
    }

    @Override
    public void endEdit(String text) {
        Value2 parsedValue = ComponentParser.parseValue(text);
        //JComponent parsedComponent = ComponentParser.parseComponent(text, playgroundView);
        endEdit(parsedValue);
    }

    protected abstract void endEdit(JComponent parsedComponent);
    protected void endEdit(Value2 parsedValue) {

    }
}
