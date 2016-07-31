package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public abstract class JsonChangeStatement {
    public abstract boolean matches(JsonChangeStatement statement, Map<String, Object> captures);
}
