package midmod.rules.patterns;

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
            public RuleMap.Node findNode(RuleMap.Node node) {
                // TODO: Implement this!!!
                // Should be a new embedded node, from which the two alternatives are added
                return null;
            }
        }

        return new OrPattern();
    }

    RuleMap.Node findNode(RuleMap.Node node);

    default RuleMap.Node findListItemNode(RuleMap.Node node) {
        return findNode(node);
    }
}
