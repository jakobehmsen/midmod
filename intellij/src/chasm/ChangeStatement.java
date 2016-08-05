package chasm;

import java.util.Map;

public abstract class ChangeStatement {
    public abstract boolean matches(ChangeStatement statement, Map<String, CapturedValue> captures);
}
