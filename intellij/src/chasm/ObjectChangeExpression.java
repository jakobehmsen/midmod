package chasm;

import java.util.Map;

public class ObjectChangeExpression extends ChangeExpression {
    private Object value;

    public ObjectChangeExpression(Object value) {
        this.value = value;
    }

    @Override
    public boolean matches(ChangeExpression expression, Map<String, CapturedValue> captures) {
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
