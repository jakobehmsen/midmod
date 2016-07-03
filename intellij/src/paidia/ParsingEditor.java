package paidia;

import javax.swing.*;

public abstract class ParsingEditor implements Editor {
    private ParseContext parseContext;
    private PlaygroundView playgroundView;

    public ParsingEditor(PlaygroundView playgroundView) {
        this.playgroundView = playgroundView;
        this.parseContext = new ParseContext() {
            @Override
            public ParameterValue newParameter() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ParsingEditor(PlaygroundView playgroundView, ParseContext parseContext) {
        this.playgroundView = playgroundView;
        this.parseContext = parseContext;
    }

    @Override
    public void endEdit(String text) {
        Value2 parsedValue = ComponentParser.parseValue(text, value -> new Value2Holder(value), parseContext);
        //JComponent parsedComponent = ComponentParser.parseComponent(text, playgroundView);
        endEdit(parsedValue);
    }

    protected abstract void endEdit(JComponent parsedComponent);
    protected void endEdit(Value2 parsedValue) {

    }
}
