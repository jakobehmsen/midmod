package midmod.rules.patterns;

import midmod.pal.CaptureConsumable;
import midmod.pal.Consumable;
import midmod.pal.ListConsumable;
import midmod.pal.ObservedConsumable;
import midmod.rules.*;
import midmod.rules.actions.Action;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Patterns {
    public static Pattern equalsObject(Object obj) {
        if(obj instanceof List)
            new String();

        class EqualsPattern implements Pattern {
            private Object theObj = obj;
            Pattern self = this;

            @Override
            public int compareInstanceTo(Pattern other) {
                return 0;
            }

            @Override
            public int sortIndex() {
                return SortIndex.EQUALS;
            }

            public boolean matchesList(Consumable value, Environment captures) {
                if(value.atEnd())
                    return false;

                boolean result = matchesSingle(value.peek(), captures);
                if(result) {
                    value.consume();
                }
                return result;
            }

            public boolean matchesSingle(Object value, Environment captures) {
                if(obj.equals("replace"))
                    new String();

                if(value.equals(obj)) {
                    return true;
                }

                return false;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof EqualsPattern && this.theObj.equals(((EqualsPattern)obj).theObj);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class EqualsEdgePattern implements EdgePattern {
                    private Object theObj = obj;

                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        return matchesList(consumable, captures) ? target : null;
                    }

                    @Override
                    public String toString() {
                        return "equals " + theObj;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof EqualsEdgePattern && this.theObj.equals(((EqualsEdgePattern)obj).theObj);
                    }
                }

                return node.byPattern(new EqualsEdgePattern());
            }

            @Override
            public Object toValue() {
                // Should obj be converted/checked as ValueConvertible?
                return Arrays.asList("equals", obj);
            }

            @Override
            public String toString() {
                return obj.toString();
            }
        }

        return new EqualsPattern();
    }

    public static Pattern binary(String operator, Class<?> lhsType, Class<?> rhsType) {
        return Patterns.subsumesList(
            Patterns.equalsObject(operator),
            Patterns.captureSingle(0, Patterns.is(lhsType)),
            Patterns.captureSingle(1, Patterns.is(rhsType))
        );
    }

    public static Pattern subsumesList(Pattern... items) {
        return subsumesList(Arrays.asList(items));
    }

    public static Pattern subsumesList(List<Pattern> list) {
        class SubsumesListPattern implements Pattern {
            List<Pattern> theList = list;
            Pattern self = this;

            @Override
            public int compareInstanceTo(Pattern other) {
                return 0;
            }

            @Override
            public int sortIndex() {
                return SortIndex.SUBSUMES_LIST;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof SubsumesListPattern && this.theList.equals(((SubsumesListPattern)obj).theList);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class SubsumesListEdgePattern implements EdgePattern {
                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        Object theValue = consumable.peek();

                        if (theValue instanceof List) {
                            List<Object> otherList = (List<Object>) theValue;
                            Consumable listConsumable = new ListConsumable(otherList);
                            listConsumable = new ObservedConsumable(listConsumable, v ->
                                consumable.propogate(v));

                            RuleMap.Node deepestNode = matchesAlternative(captures, listConsumable, local, target);
                            if(deepestNode != null/* && listConsumable.atEnd()*/)
                                return deepestNode;

                            /*for(Map.Entry<EdgePattern, RuleMap.Node> e: target.edges()) {
                                RuleMap.Node an = matchesAlternative(captures, listConsumable, e, local);
                                if(an != null) { //&& listConsumable.atEnd()
                                    consumable.consume();
                                    return an;
                                }
                            }*/
                        }

                        return null;
                    }

                    private RuleMap.Node matchesAlternative(Environment innerCaptures, Consumable consumable, RuleMap local, RuleMap.Node node) {
                        for (Map.Entry<EdgePattern, RuleMap.Node> e : node.edges()) {
                            consumable.mark();
                            innerCaptures.mark();
                            RuleMap.Node n = e.getKey().matches(e.getValue(), consumable, innerCaptures, local);
                            if(n != null) {
                                RuleMap.Node an = matchesAlternative(innerCaptures, consumable, local, n);
                                if (an != null) {
                                    innerCaptures.commit();
                                    consumable.commit();
                                    return an;
                                }
                            }
                            consumable.rollback();
                            innerCaptures.rollback();
                        }

                        // Found node at or after consumed everything
                        if(consumable.atEnd())
                            return node;

                        return null;
                    }

                    private RuleMap.Node matchesAlternative(Environment innerCaptures, Consumable consumable, Map.Entry<EdgePattern, RuleMap.Node> edge, RuleMap local) {
                        consumable.mark();
                        innerCaptures.mark();
                        RuleMap.Node n = edge.getKey().matches(edge.getValue(), consumable, innerCaptures, local);
                        if(n != null) {
                            if(consumable.atEnd()) {
                                innerCaptures.commit();
                                consumable.commit();
                                return n;
                            }

                            for(Map.Entry<EdgePattern, RuleMap.Node> e: n.edges()) {
                                RuleMap.Node an = matchesAlternative(innerCaptures, consumable, e, local);
                                if(an != null) {
                                    innerCaptures.commit();
                                    consumable.commit();
                                    return an;
                                }
                            }
                        }

                        consumable.rollback();
                        innerCaptures.rollback();

                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SubsumesListEdgePattern;
                    }

                    @Override
                    public String toString() {
                        return "subsumes list ";
                    }
                }

                RuleMap.Node listNode = node.byPattern(new SubsumesListEdgePattern());

                RuleMap.Node n = listNode;

                for (Pattern pattern : list)
                    n = pattern.findListItemNode(n);

                return n;
            }

            @Override
            public RuleMap.Node findListItemNode(RuleMap.Node node) {
                RuleMap.Node pseudoNode = new RuleMap.Node();

                RuleMap.Node endNode = findNode(pseudoNode);

                return node.byPattern(new EdgePattern() {
                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        if(consumable.atEnd())
                            return null;

                        Object theValue = consumable.peek();
                        Consumable theValueAsConsumable = new ListConsumable(Arrays.asList(theValue));

                        RuleMap.Node n = pseudoNode.match(theValueAsConsumable, captures, local);

                        if(n == endNode) {
                            consumable.consume();

                            return target;
                        }

                        return null;
                    }
                });
            }

            @Override
            public Object toValue() {
                return Arrays.asList("subsumes-list", list.stream().map(x -> x.toValue()).collect(Collectors.toList()));
            }

            @Override
            public String toString() {
                return list.toString();
            }
        }

        return new SubsumesListPattern();
    }

    public static Pattern is(Class<?> type) {
        if(type == null)
            new String();

        class IsPattern implements Pattern {
            Class<?> theType = type;
            Pattern self = this;

            @Override
            public int compareInstanceTo(Pattern other) {
                return 0;
            }

            @Override
            public int sortIndex() {
                return SortIndex.IS;
            }

            public boolean matchesList(Consumable value, Environment captures) {
                boolean result = matchesSingle(value.peek(), captures);
                if(result) {
                    value.consume();
                }
                return result;
            }

            public boolean matchesSingle(Object value, Environment captures) {
                if(type.isInstance(value)) {
                    return true;
                }

                return false;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof IsPattern && theType.equals(((IsPattern)obj).theType);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class IsEdgePattern implements EdgePattern {

                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        return matchesList(consumable, captures) ? target : null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof IsEdgePattern && theType.equals(((IsPattern)((IsEdgePattern)obj).pattern()).theType);
                    }

                    @Override
                    public String toString() {
                        return "is " + type;
                    }
                }

                return node.byPattern(new IsEdgePattern());
            }

            @Override
            public Object toValue() {
                return Arrays.asList("is", type);
            }

            @Override
            public String toString() {
                if(type.equals(String.class))
                    return "%s";
                else if(type.equals(int.class))
                    return "%i";
                return "<NA>";
            }
        }

        return new IsPattern();
    }

    public static Pattern subsumesToMap(List<Map.Entry<String, Pattern>> map) {
        class SubsumesMapPattern implements Pattern {
            List<Map.Entry<String, Pattern>> theMap = map;

            private boolean isMoreGeneral(List<Map.Entry<String, Pattern>> moreGeneralTest, List<Map.Entry<String, Pattern>> lessGeneralTest) {
                return lessGeneralTest.stream().allMatch(lessGeneralEntry -> {
                    Optional<Map.Entry<String, Pattern>> moreGeneralEntry = moreGeneralTest.stream().filter(y -> y.getKey().equals(lessGeneralEntry.getKey())).findFirst();

                    return !moreGeneralEntry.isPresent() || lessGeneralEntry.getValue().compareTo(moreGeneralEntry.get().getValue()) <= 0;
                }) && lessGeneralTest.size() >= moreGeneralTest.size();
            }

            @Override
            public int compareInstanceTo(Pattern other) {
                if(isMoreGeneral(theMap, ((SubsumesMapPattern)other).theMap))
                    return 1;
                else if(isMoreGeneral(((SubsumesMapPattern)other).theMap, theMap))
                    return -1;

                return 0;
            }

            @Override
            public int sortIndex() {
                return SortIndex.SUBSUMES_MAP;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof SubsumesMapPattern && this.theMap.equals(((SubsumesMapPattern)obj).theMap);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class SubsumesMapEdgePattern implements EdgePattern {
                    Map<String, RuleMap.Node> nodes = map.stream()
                        .collect(Collectors.toMap(x -> x.getKey(), x -> {
                            RuleMap.Node n = new RuleMap.Node();
                            x.getValue().findNode(n);
                            return n;
                        }));

                    @Override
                    public Pattern pattern() {
                        return SubsumesMapPattern.this;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        if(consumable.peek() instanceof Map) {
                            Map<String, Object> otherMap = (Map<String, Object>) consumable.peek();
                            boolean subsumes = map.stream()
                                .allMatch(e -> {
                                    Object slotValue = otherMap.get(e.getKey());

                                    if(slotValue != null) {
                                        // Wrap into Consumable
                                        Consumable slotValueAsConsumable = new ListConsumable(Arrays.asList(slotValue));
                                        RuleMap.Node node = nodes.get(e.getKey());
                                        return node.match(slotValueAsConsumable, captures, local) != null;
                                    }
                                    return false;
                                });

                            if(subsumes) {
                                consumable.consume();
                                return target;
                            }
                            return null;
                        }

                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof SubsumesMapEdgePattern && SubsumesMapPattern.this.theMap.equals(((SubsumesMapPattern)((SubsumesMapEdgePattern)obj).pattern()).theMap);
                    }
                }

                return node.byPattern(new SubsumesMapEdgePattern());
            }

            @Override
            public Object toValue() {
                return Arrays.asList("subsumes-map", map.stream().map(x -> Arrays.asList(x.getKey(), x.getValue().toValue())).collect(Collectors.toList()));
            }

            @Override
            public String toString() {
                return "{" +
                    map.stream().map(x -> x.getKey().toString() + ": " + x.getValue().toString()).collect(Collectors.joining(", ")) +
                    "}";
            }
        }

        return new SubsumesMapPattern();
    }


    public static Pattern captureSingle(int index, Pattern pattern) {
        return capture(index, pattern, captured -> captured.get(0));
    }

    public static Pattern captureMany(int index, Pattern pattern) {
        return capture(index, pattern, captured -> captured);
    }

    public static Pattern capture(int index, Pattern pattern, Function<List<Object>, Object> valueExtractor) {
        class CapturePattern implements Pattern {
            Pattern thePattern = pattern;

            @Override
            public int compareInstanceTo(Pattern other) {
                if(other instanceof CapturePattern)
                    return this.thePattern.compareInstanceTo(((CapturePattern)other).thePattern);
                return this.thePattern.compareInstanceTo(other);
            }

            @Override
            public int sortIndex() {
                return pattern.sortIndex();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof CapturePattern && this.thePattern.equals(((CapturePattern)obj).thePattern);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class CaptureEdgePattern implements EdgePattern {
                    RuleMap.Node pseudoNode = new RuleMap.Node();
                    RuleMap.Node endNode = thePattern.findNode(pseudoNode);

                    @Override
                    public Pattern pattern() {
                        return CapturePattern.this;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        /*
                        Changed captures parameter into List in which, arguments are bound for actions
                        Don't use environment for capturing; decorate consumable and put the consumed
                        values into captures List at the given index.
                        - Could be prettyfied
                        */

                        CaptureConsumable captureConsumable = new CaptureConsumable(consumable);

                        Consumable c = new ObservedConsumable(captureConsumable, v ->
                            consumable.propogate(v));

                        RuleMap.Node n = pseudoNode.match(c, captures, local);

                        if(n == endNode) {
                            Object valueToCapture = valueExtractor.apply(captureConsumable.getCapturedElements());
                            captures.capture(index, valueToCapture);

                            return target;
                        }

                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof CaptureEdgePattern && this.pseudoNode.getEdge(this.endNode).equals(((CaptureEdgePattern) obj).pseudoNode.getEdge(((CaptureEdgePattern) obj).endNode));
                    }

                    @Override
                    public String toString() {
                        return "captures " + index;
                    }
                }

                return node.byPattern(new CaptureEdgePattern());
            }

            @Override
            public Object toValue() {
                // TODO: How to convert valueExtractor?
                return Arrays.asList("capture", index, pattern.toValue());
            }

            @Override
            public String toString() {
                return pattern + " " + index;
            }
        }

        return new CapturePattern();
    }

    public static Pattern reference(RuleMap rules, String name) {
        class ReferencePattern implements Pattern {
            private String theName = name;

            private Rule resolveRule() {
                Environment captures = new Environment();

                Action action = rules.resolve(name, captures, rules);

                return ((Rule) action.perform(rules, rules, captures));
            }

            private Pattern resolvePattern() {
                return resolveRule().getPattern();
            }

            @Override
            public int compareInstanceTo(Pattern other) {
                // What if the value of the rules for name changes?
                // Should it be possible to observe the rules for changes and then notisfy outwards when changes occur?
                Pattern pattern = resolvePattern();
                return pattern.compareInstanceTo(other);
                /*Pattern patternOther = ((ReferencePattern)other).resolvePattern();

                return pattern.compareInstanceTo(patternOther);*/
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ReferencePattern && this.theName.equals(((ReferencePattern)obj).theName);
            }

            @Override
            public int sortIndex() {
                // What if the value of the rules for name changes?
                // Should it be possible to observe the rules for changes and then notisfy outwards when changes occur?
                Pattern pattern = resolvePattern();

                return pattern.sortIndex();
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class ReferenceEdgePattern implements EdgePattern {
                    RuleMap.Node pseudoNode = new RuleMap.Node();
                    RuleMap.Node patternNode = resolvePattern().findNode(pseudoNode);

                    @Override
                    public Pattern pattern() {
                        return ReferencePattern.this;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        Environment innerCaptures = new Environment();
                        RuleMap.Node n =  pseudoNode.match(consumable, innerCaptures, local);

                        if(n != null) {
                            Object result = resolveRule().getAction().perform(rules, local, innerCaptures);
                            consumable.propogate(result);
                            return target;
                        }

                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof ReferenceEdgePattern && this.pseudoNode.getEdge(this.patternNode).equals(((ReferenceEdgePattern) obj).pseudoNode.getEdge(((ReferenceEdgePattern) obj).patternNode));
                    }
                }

                return node.byPattern(new ReferenceEdgePattern());
            }

            @Override
            public Object toValue() {
                return Arrays.asList("reference", name);
            }

            @Override
            public String toString() {
                return name;
            }
        }

        return new ReferencePattern();
    }

    public static Pattern subsumesToRuleMap(List<Pattern> patterns) {
        class RuleMapPattern implements Pattern {
            private List<Pattern> thePatterns = patterns;

            @Override
            public int compareInstanceTo(Pattern other) {
                // How to compare rule map patterns?
                return 0;
            }

            @Override
            public int sortIndex() {
                return SortIndex.SUBSUMES_RULE_MAP;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof RuleMapPattern && this.thePatterns.equals(((RuleMapPattern)obj).thePatterns);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class RuleMapEdgePattern implements EdgePattern {
                    @Override
                    public Pattern pattern() {
                        return RuleMapPattern.this;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        if(consumable.peek() instanceof RuleMap) {
                            RuleMap otherRuleMap = (RuleMap) consumable.peek();
                            boolean subsumes = patterns.stream()
                                .allMatch(e ->
                                    otherRuleMap.defines(e));
                            if(subsumes) {
                                consumable.consume();;
                                return target;
                            }
                            return null;
                        }

                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof RuleMapEdgePattern && pattern().equals(((RuleMapEdgePattern)obj).pattern());
                    }
                }

                return node.byPattern(new RuleMapEdgePattern());
            }

            @Override
            public Object toValue() {
                return Arrays.asList("rule-map", patterns.stream().map(x -> x.toValue()).collect(Collectors.toList()));
            }

            @Override
            public String toString() {
                return "{" + patterns.stream().map(x -> x.toString()).collect(Collectors.joining(" ")) + "}";
            }
        }

        return new RuleMapPattern();
    }

    private static class AnyPattern implements Pattern {
        Pattern self = this;

        @Override
        public int compareInstanceTo(Pattern other) {
            return 0;
        }

        @Override
        public int sortIndex() {
            return SortIndex.ANY;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AnyPattern;
        }

        @Override
        public RuleMap.Node findNode(RuleMap.Node node) {
            class AnythingEdgePattern implements EdgePattern {
                @Override
                public Pattern pattern() {
                    return self;
                }

                @Override
                public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                    Object val = consumable.peek();
                    consumable.consume();

                    return target;
                }

                @Override
                public boolean equals(Object obj) {
                    return obj instanceof AnythingEdgePattern;
                }

                @Override
                public String toString() {
                    return "_";
                }
            }

            return node.byPattern(new AnythingEdgePattern());
        }

        @Override
        public Object toValue() {
            return Arrays.asList("any");
        }

        @Override
        public String toString() {
            return "_";
        }
    }

    public static final Pattern anything = new AnyPattern();

    public static Pattern repeat(Pattern pattern) {
        class RepeatPattern implements Pattern {
            Pattern thePattern = pattern;
            Pattern self = this;

            @Override
            public int compareInstanceTo(Pattern other) {
                return thePattern.compareTo(((RepeatPattern)other).thePattern);
            }

            @Override
            public int sortIndex() {
                return SortIndex.REPEAT;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof RepeatPattern && this.thePattern.equals(((RepeatPattern)obj).thePattern);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class RepeatEdgePattern implements EdgePattern {
                    RuleMap.Node pseudoNode = new RuleMap.Node();
                    RuleMap.Node patternNode = pattern.findNode(pseudoNode);

                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        Environment innerCaptures = new Environment();

                        while(!consumable.atEnd()) {
                            RuleMap.Node n = pseudoNode.match(consumable, innerCaptures, local);
                            if(n != patternNode)
                                break;
                        }

                        return target;
                    }

                    @Override
                    public String toString() {
                        return pattern + " ...";
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof RepeatEdgePattern && this.pseudoNode.getEdge(this.patternNode).equals(((RepeatEdgePattern) obj).pseudoNode.getEdge(((RepeatEdgePattern) obj).patternNode));
                    }
                }

                RuleMap.Node repeatNode = node.byPattern(new RepeatEdgePattern());

                return repeatNode;
            }

            @Override
            public Object toValue() {
                return Arrays.asList("repeat", pattern.toValue());
            }

            @Override
            public String toString() {
                return thePattern + "...";
            }
        }

        return new RepeatPattern();
    }

    public static Pattern not(Pattern patternToNegate) {
        class NotPattern implements Pattern {
            Pattern thePattern = patternToNegate;
            Pattern self = this;

            @Override
            public int compareInstanceTo(Pattern other) {
                return patternToNegate.compareTo(((NotPattern)other).thePattern) * -1;
            }

            @Override
            public int sortIndex() {
                return SortIndex.NOT;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof NotPattern && this.thePattern.equals(((NotPattern)obj).thePattern);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class NotEdgePattern implements EdgePattern {
                    RuleMap.Node pseudoNode = new RuleMap.Node();
                    RuleMap.Node patternToNegateNode = patternToNegate.findNode(pseudoNode);
                    Pattern thePatternToNegate = patternToNegate;

                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures, RuleMap local) {
                        consumable.mark();

                        RuleMap.Node n = pseudoNode.match(consumable, captures, local);

                        consumable.rollback();

                        if(n == null) {
                            // What if embedded pattern is repeat pattern? Should that be possible?
                            // Or only single-capturing patterns?
                            consumable.consume();

                            return target;
                        }
                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof NotEdgePattern && this.pseudoNode.getEdge(this.patternToNegateNode).equals(((NotEdgePattern)obj).pseudoNode.getEdge(((NotEdgePattern)obj).patternToNegateNode));
                    }
                }

                return node.byPattern(new NotEdgePattern());
            }

            @Override
            public Object toValue() {
                return Arrays.asList("not", patternToNegate.toValue());
            }

            @Override
            public String toString() {
                return "!" + thePattern;
            }
        }

        return new NotPattern();
    }
}
