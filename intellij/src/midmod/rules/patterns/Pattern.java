package midmod.rules.patterns;

import midmod.pal.Consumable;
import midmod.rules.EdgePattern;
import midmod.rules.Environment;
import midmod.rules.RuleMap;

public interface Pattern {
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
                //return Math.max(self.sortIndex(), other.sortIndex());

                return SortIndex.OR;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class OrEdgePattern implements EdgePattern {
                    RuleMap.Node contentNode = new RuleMap.Node();
                    RuleMap.Node firstNode = self.findNode(contentNode);
                    RuleMap.Node secondNode = other.findNode(contentNode);

                    @Override
                    public Pattern pattern() {
                        return OrPattern.this;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures) {
                        /*consumable.mark();
                        captures.mark();

                        RuleMap.Node n = firstNode.match(consumable, captures);

                        if(n != null) {
                            consumable.commit();
                            captures.commit();

                            return target;
                        }

                        consumable.rollback();
                        captures.rollback();

                        n = secondNode.match(consumable, captures);

                        return n != null ? target : null;*/

                        RuleMap.Node n = contentNode.match(consumable, captures);
                        return n != null ? target : null;
                    }
                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof OrEdgePattern &&
                            this.contentNode.getEdge(this.firstNode).equals(((OrEdgePattern)obj).contentNode.getEdge(((OrEdgePattern)obj).firstNode)) &&
                            this.contentNode.getEdge(this.secondNode).equals(((OrEdgePattern)obj).contentNode.getEdge(((OrEdgePattern)obj).secondNode));
                    }
                }

                return node.byPattern(new OrEdgePattern());

                /*
                // TODO: Implement this!!!
                // Should be a new embedded node, from which the two alternatives are added
                return null;
                */
            }
        }

        return new OrPattern();
    }

    RuleMap.Node findNode(RuleMap.Node node);

    default RuleMap.Node findListItemNode(RuleMap.Node node) {
        return findNode(node);
    }
}
