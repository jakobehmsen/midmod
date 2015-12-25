package midmod.rules.patterns;

import midmod.pal.Consumable;
import midmod.rules.RuleMap;

import java.util.Map;

public interface Pattern {
    boolean matchesList(Consumable value, Map<String, Object> captures);
    boolean matchesSingle(Object value, Map<String, Object> captures);

    default Pattern or(Pattern other) {
        Pattern self = this;

        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                value.mark();

                if(self.matchesList(value, captures)) {
                    value.commit();
                    return true;
                }

                value.rollback();

                return other.matchesList(value, captures);
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                return self.matchesSingle(value, captures) || other.matchesSingle(value, captures);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                return null;
            }
        };
    }

    RuleMap.Node findNode(RuleMap.Node node);
}
