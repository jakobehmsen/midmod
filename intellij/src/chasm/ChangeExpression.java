package chasm;

import java.util.List;
import java.util.Map;

public abstract class ChangeExpression {
    public abstract boolean matches(ChangeExpression expression, Map<String, List<Object>> captures);
}
