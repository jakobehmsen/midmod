package chasm;

import java.util.List;
import java.util.Map;

public class ThisExpression extends JsonChangeExpression {
    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, List<Object>> captures) {
        return expression instanceof ThisExpression;
    }

    @Override
    public String toString() {
        return "this";
    }
}
