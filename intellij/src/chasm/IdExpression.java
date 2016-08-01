package chasm;

import java.util.List;
import java.util.Map;

public abstract class IdExpression {
    public abstract boolean matches(IdExpression idExpression, Map<String, List<Object>> captures);
}
