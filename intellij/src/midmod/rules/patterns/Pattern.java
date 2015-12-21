package midmod.rules.patterns;

import midmod.pal.Consumable;

import java.util.Map;

public interface Pattern {
    boolean matchesList(Consumable value, Map<String, Object> captures);
    boolean matchesSingle(Object value, Map<String, Object> captures);

    default Pattern andThen(Pattern next) {
        Pattern self = this;

        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                return self.matchesList(value, captures) && next.matchesList(value, captures);
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                return self.matchesSingle(value, captures) && next.matchesSingle(value, captures);
            }
        };
    }

    default Pattern or(Pattern other) {
        Pattern self = this;

        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                return self.matchesList(value, captures) || other.matchesList(value, captures);
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                return self.matchesSingle(value, captures) || other.matchesSingle(value, captures);
            }
        };
    }
}
