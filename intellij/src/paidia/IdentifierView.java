package paidia;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class IdentifierView extends JLabel implements ValueView {
    public IdentifierView(String name) {
        setText(name);
        setSize(getPreferredSize());
    }

    @Override
    public String getText(TextContext textContext) {
        return getText();
    }

    @Override
    public void setup(PlaygroundView playgroundView) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return environment.get(getText());
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

    @Override
    public void appendIdentifiers(List<String> identifiers) {
        if(!identifiers.contains(getText()))
            identifiers.add(getText());
    }
}
