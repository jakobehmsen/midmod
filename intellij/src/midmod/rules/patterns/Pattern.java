package midmod.rules.patterns;

import midmod.pal.Consumable;
import midmod.rules.Environment;
import midmod.rules.RuleMap;

public interface Pattern extends Comparable<Pattern> {
    default int compareTo(Pattern other) {
        int deltaSortIndex = sortIndex() - other.sortIndex();

        if(deltaSortIndex == 0)
            return compareInstanceTo(other);

        return deltaSortIndex;
    }

    int compareInstanceTo(Pattern other);

    int sortIndex();

    boolean matchesList(Consumable value, Environment captures);
    boolean matchesSingle(Object value, Environment captures);

    default Pattern or(Pattern other) {
        Pattern self = this;

        class OrPattern implements Pattern {
            Pattern theSelf = self;
            Pattern theOther = other;

            @Override
            public int compareInstanceTo(Pattern other) {
                int selfCompare = theSelf.compareTo(((OrPattern)other).theSelf);
                int selfOther = theOther.compareTo(((OrPattern)other).theOther);

                return selfCompare + selfOther;
            }

            @Override
            public int sortIndex() {
                return Math.max(self.sortIndex(), other.sortIndex());
            }

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
        }

        return new OrPattern();
    }

    RuleMap.Node findNode(RuleMap.Node node);

    //RuleMap.Node matches(RuleMap.Node node, Object value, Map<String, Object> captures);

    default RuleMap.Node findListItemNode(RuleMap.Node node) {
        return findNode(node);
    }
}
