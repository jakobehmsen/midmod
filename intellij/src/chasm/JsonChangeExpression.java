package chasm;

import java.util.List;
import java.util.Map;

public abstract class JsonChangeExpression {
    public abstract boolean matches(JsonChangeExpression expression, Map<String, List<Object>> captures);
}
