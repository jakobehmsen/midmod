package chasm;

import java.util.Map;

public class ThisChangeExpression extends ChangeExpression {
    @Override
    public boolean matches(ChangeExpression expression, Map<String, CapturedValue> captures) {
        return expression instanceof ThisChangeExpression;
    }

    @Override
    public String toString() {
        return "this";
    }
}
