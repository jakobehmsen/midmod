package paidia;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class IdentifierValue2 extends AbstractValue2 {
    private String name;

    public IdentifierValue2(String name) {
        this.name = name;
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JLabel label = new JLabel(name);

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return label;
            }
        };
    }

    @Override
    public String getText() {
        return name;
    }

    @Override
    public String getSource(TextContext textContext) {
        return name;
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return environment.get(name);
    }

    @Override
    public void appendParameters(List<String> parameters) {
        if(!parameters.contains(name))
            parameters.add(name);
    }
}
