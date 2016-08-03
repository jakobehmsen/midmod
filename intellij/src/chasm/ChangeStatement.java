package chasm;

import java.util.List;
import java.util.Map;

public abstract class ChangeStatement {
    public abstract boolean matches(ChangeStatement statement, Map<String, List<Object>> captures);
}
