package paidia;

import javax.swing.*;

public class AtomView extends JLabel implements ValueView {
    public AtomView(String text) {
        setText(text);
        setSize(getPreferredSize());
    }

    @Override
    public String getText(TextContext textContext) {
        return getText();
    }

    @Override
    public void setup(PlaygroundView playgroundView) {

    }
}
