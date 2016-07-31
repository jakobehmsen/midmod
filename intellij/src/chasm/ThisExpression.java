package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public class ThisExpression extends JsonChangeExpression {
    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, Object> captures) {
        return expression instanceof ThisExpression;
    }

    @Override
    public String toString() {
        return "this";
    }
}
