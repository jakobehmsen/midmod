package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public class ObjectChangeExpression extends JsonChangeExpression {
    private Object value;

    public ObjectChangeExpression(Object value) {
        this.value = value;
    }

    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, Object> captures) {
        if(expression instanceof ObjectChangeExpression) {
            ObjectChangeExpression objChangeExpression = (ObjectChangeExpression)expression;
            return this.value.equals(objChangeExpression.value);
        }

        return false;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
