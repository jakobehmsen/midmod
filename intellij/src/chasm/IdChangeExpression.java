package chasm;

import java.util.Map;

public abstract class IdChangeExpression {
    public abstract boolean matches(IdChangeExpression idExpression, Map<String, CapturedValue> captures);
}
