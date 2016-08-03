package chasm;

import java.util.List;
import java.util.Map;

public abstract class IdChangeExpression {
    public abstract boolean matches(IdChangeExpression idExpression, Map<String, List<Object>> captures);
}
