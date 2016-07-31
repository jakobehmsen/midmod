package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public abstract class JsonChangeExpression {
    public abstract boolean matches(JsonChangeExpression expression, Map<String, Object> captures);
}
