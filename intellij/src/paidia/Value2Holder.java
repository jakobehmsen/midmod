package paidia;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class Value2Holder extends AbstractValue2 implements Value2Observer {
    private Value2 value;

    public Value2Holder(Value2 value) {
        this.value = value;
        this.value.addObserver(this);
    }

    public void setValue(Value2 value) {
        this.value.removeObserver(this);
        this.value = value;
        this.value.addObserver(this);
        sendUpdated();
    }

    public Value2 getValue() {
        return value;
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        Value2ViewWrapper value2ViewWrapper = new Value2ViewWrapper(this, value.toView(playgroundView).getComponent());

        playgroundView.makeEditableByMouse(value2ViewWrapper);

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return value2ViewWrapper;
            }
        };

        //return new Value2ViewWrapper(this);
    }

    @Override
    public String getText() {
        return value.getText();
    }

    @Override
    public String getSource(TextContext textContext) {
        return value.getSource(textContext);
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return value.reduce(environment);
    }

    @Override
    public void updated() {
        sendUpdated();
    }

    @Override
    public void appendParameters(List<String> parameters) {
        value.appendParameters(parameters);
    }
}
