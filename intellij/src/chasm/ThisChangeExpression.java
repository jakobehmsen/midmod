package chasm;

import java.util.List;
import java.util.Map;

public class ThisChangeExpression extends ChangeExpression {
    @Override
    public boolean matches(ChangeExpression expression, Map<String, List<Object>> captures) {
        return expression instanceof ThisChangeExpression;
    }

    @Override
    public String toString() {
        return "this";
    }
}
