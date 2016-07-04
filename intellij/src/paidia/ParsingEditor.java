package paidia;

import javax.swing.*;

public abstract class ParsingEditor implements Editor {
    private ParseContext parseContext;
    private PlaygroundView playgroundView;

    private static class FrameIdProvider implements IdProvider {
        private int frameId;
        private int slotId;
        private PlaygroundView playgroundView;

        private FrameIdProvider(PlaygroundView playgroundView) {
            this.frameId = playgroundView.nextFrameId();
            this.playgroundView= playgroundView;
        }

        @Override
        public IdProvider forNewFrame() {
            return new FrameIdProvider(playgroundView);
        }

        @Override
        public String nextId() {
            return frameId + "-" + (slotId++);
        }
    }

    public ParsingEditor(PlaygroundView playgroundView) {
        this.playgroundView = playgroundView;
        this.parseContext = new ParseContext() {
            @Override
            public ParameterValue newParameter() {
                throw new UnsupportedOperationException();
            }

            @Override
            public IdProvider newIdProviderForFrame() {
                return new FrameIdProvider(playgroundView);
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
