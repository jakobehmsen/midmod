package chasm;

import java.util.Map;

public abstract class ChangeExpression {
    public abstract boolean matches(ChangeExpression expression, Map<String, CapturedValue> captures);
}
