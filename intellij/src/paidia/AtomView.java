package paidia;

import javax.swing.*;

public class AtomView extends JLabel implements ValueView {
    private Object value;

    public AtomView(String text, Object value) {
        setText(text);
        setSize(getPreferredSize());
        this.value = value;
    }

    @Override
    public String getText(TextContext textContext) {
        return getText();
    }

    @Override
    public void setup(PlaygroundView playgroundView) {

    }

    @Override
    public ValueView reduce() {
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
