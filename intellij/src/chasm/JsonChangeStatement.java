package chasm;

import java.util.List;
import java.util.Map;

public abstract class JsonChangeStatement {
    public abstract boolean matches(JsonChangeStatement statement, Map<String, List<Object>> captures);
}
