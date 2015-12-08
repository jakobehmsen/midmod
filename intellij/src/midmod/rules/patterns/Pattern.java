package midmod.rules.patterns;

import java.util.Map;

public interface Pattern {
    boolean matches(Object value, Map<String, Object> captures);
}
