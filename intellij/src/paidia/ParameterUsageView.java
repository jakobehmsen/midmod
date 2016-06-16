package paidia;

import javax.swing.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class ParameterUsageView extends JLabel implements ValueView {
    public ParameterUsageView(String name) {
        setText(name);

        Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
        fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        setFont(getFont().deriveFont(fontAttributes));
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
        return null;
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
}
