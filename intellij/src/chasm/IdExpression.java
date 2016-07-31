package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public abstract class IdExpression {
    public abstract boolean matches(IdExpression idExpression, Map<String, Object> captures);
}
