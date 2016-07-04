package paidia;

import javax.swing.*;
import java.util.Map;

public class AtomValue2 extends AbstractValue2 {
    private String text;
    private String source;
    private Object value;

    public AtomValue2(String text, String source, Object value) {
        this.text = text;
        this.source = source;
        this.value = value;
    }

    public AtomValue2(Object value) {
        this(value.toString(), value.toString(), value);
    }


    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JLabelValueView2 label = new JLabelValueView2(text);

        //playgroundView.makeEditableByMouse(label);
        label.setSize(label.getPreferredSize());

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return label;
            }
        };
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getSource(TextContext textContext) {
        return source;
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return this;
    }

    public Object getValue() {
        return value;
    }
}
