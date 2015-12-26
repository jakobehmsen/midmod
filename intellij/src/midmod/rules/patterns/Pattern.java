package midmod.rules.patterns;

import midmod.pal.Consumable;
import midmod.rules.Environment;
import midmod.rules.RuleMap;

public interface Pattern /*extends Comparable<Pattern>*/ {
    /*default int compareTo(Pattern other) {
        return sortIndex() - other.sortIndex();
    }

    default int sortIndex() {
        throw new UnsupportedOperationException();
    }*/

    boolean matchesList(Consumable value, Environment captures);
    boolean matchesSingle(Object value, Environment captures);

    default Pattern or(Pattern other) {
        Pattern self = this;

        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                value.mark();

                if(self.matchesList(value, captures)) {
                    value.commit();
                    return true;
                }

                value.rollback();

                return other.matchesList(value, captures);
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                return self.matchesSingle(value, captures) || other.matchesSingle(value, captures);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                return null;
            }

            /*@Override
            public RuleMap.Node matches(RuleMap.Node node, Object value, Map<String, Object> captures) {
                return null;
            }*/
        };
    }

    RuleMap.Node findNode(RuleMap.Node node);

    //RuleMap.Node matches(RuleMap.Node node, Object value, Map<String, Object> captures);
}
