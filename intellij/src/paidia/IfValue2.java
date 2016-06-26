package paidia;

import java.util.Map;

public class IfValue2 extends AbstractValue2 {
    public IfValue2(Value2 condition, Value2 trueExpression, Value2 falseExpression) {
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        return null;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public String getSource(TextContext textContext) {
        return null;
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return null;
    }
}
