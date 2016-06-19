package paidia;

import javax.swing.*;
import java.util.Map;

public class AtomView extends JLabel implements ValueView {
    private Object value;
    private String source;

    public AtomView(String text, String source, Object value) {
        this.source = source;
        this.value = value;
        setText(text);
        setSize(getPreferredSize());
    }

    public AtomView(String text, Object value) {
        this(text, text, value);
    }

    public AtomView(Object value) {
        this(value.toString(), value);
    }

    @Override
    public String getSource(TextContext textContext) {
        return source;
    }

    @Override
    public void setup(PlaygroundView playgroundView) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return this;
    }

    @Override
    public void addObserver(ValueViewObserver observer) {

    }

    @Override
    public void removeObserver(ValueViewObserver observer) {

    }

    @Override
    public void release() {

    }

    public Object getValue() {
        return value;
    }
}
