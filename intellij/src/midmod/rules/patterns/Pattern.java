package midmod.rules.patterns;

import java.util.Map;

public interface Pattern {
    boolean matches(Object value, Map<String, Object> captures);

    default Pattern andThen(Pattern next) {
        return (value, captures) ->
            this.matches(value, captures) && next.matches(value, captures);
    }

    default Pattern or(Pattern other) {
        return (value, captures) ->
            this.matches(value, captures) || other.matches(value, captures);
    }
}
