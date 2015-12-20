package midmod.rules.patterns;

import midmod.pal.Consumable;

import java.util.Map;

public interface Pattern {
    boolean matches(Consumable value, Map<String, Object> captures);

    default Pattern andThen(Pattern next) {
        return (value, captures) ->
            this.matches(value, captures) && next.matches(value, captures);
    }

    default Pattern or(Pattern other) {
        return (value, captures) ->
            this.matches(value, captures) || other.matches(value, captures);
    }
}
