package midmod.rules.patterns;

import midmod.pal.CaptureConsumable;
import midmod.pal.Consumable;
import midmod.pal.ListConsumable;
import midmod.pal.ObservedConsumable;
import midmod.rules.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Patterns {
    public static Pattern equalsObject(Object obj) {
        class EqualsPattern implements Pattern {
            Pattern self = this;

            @Override
            public int compareInstanceTo(Pattern other) {
                return 0;
            }

            @Override
            public int sortIndex() {
                return 0;
            }

            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                boolean result = matchesSingle(value.peek(), captures);
                if(result) {
                    captures.captureSingle(value.peek());
                    value.consume();
                }
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                if(obj.equals("replace"))
                    new String();

                if(value.equals(obj)) {
                    return true;
                }

                return false;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                return node.byPattern(new EdgePattern() {
                    private Object theObj = obj;
                    private Field theObjField;

                    {
                        try {
                            theObjField = getClass().getDeclaredField("theObj");
                            theObjField.setAccessible(true);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        if(value instanceof Consumable)
                            return matchesList(((Consumable) value), captures) ? target : null;

                        RuleMap.Node n = matchesSingle(value, captures) ? target : null;
                        if(n != null) {
                            captures.captureSingle(value);
                        }
                        return n;
                    }

                    @Override
                    public String toString() {
                        return "equals " + theObj;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        try {
                            return getClass().isInstance(obj) && theObj.equals(theObjField.get(obj));
                        } catch (IllegalAccessException e) {
                            return false;
                        }
                    }
                });
            }
        }

        return new EqualsPattern();
    }

    public static Pattern binary(String operator, Class<?> lhsType, Class<?> rhsType) {
        return Patterns.conformsTo(
            Patterns.equalsObject(operator),
            Patterns.is(lhsType),
            Patterns.is(rhsType)
        );
    }

    public static Pattern conformsTo(Pattern... items) {
        return conformsTo(Arrays.asList(items));
    }

    public static Pattern conformsTo(List<Pattern> list) {
        class SubsumesList implements Pattern {
            Pattern self = this;

            @Override
            public int compareInstanceTo(Pattern other) {
                return 0;
            }

            @Override
            public int sortIndex() {
                return 1;
            }

            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                boolean result = matchesSingle(value.peek(), captures);
                value.consume();
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                if(value instanceof List) {
                    List<Object> otherList = (List<Object>) value;
                    Consumable listConsumable = new ListConsumable(otherList);
                    return IntStream.range(0, list.size())
                        .allMatch(i ->
                            list.get(i).matchesList(listConsumable, captures))
                        && listConsumable.atEnd();
                }

                return false;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                RuleMap.Node listNode = node.byPattern(new EdgePattern() {
                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        Object theValue = value instanceof Consumable ? ((Consumable)value).peek() : value;

                        if (theValue instanceof List) {
                            List<Object> otherList = (List<Object>) theValue;
                            Consumable listConsumable = new ListConsumable(otherList);
                            if(value instanceof Consumable)
                                listConsumable = new ObservedConsumable(listConsumable, v ->
                                    ((Consumable)value).propogate(v));

                            for(Map.Entry<EdgePattern, RuleMap.Node> e: target.edges()) {
                                RuleMap.Node an = matchesAlternative(captures, listConsumable, e);
                                if(an != null && listConsumable.atEnd()) {
                                    if(value instanceof Consumable)
                                        ((Consumable)value).consume();
                                    return an;
                                }
                            }
                        }

                        return null;
                    }

                    private RuleMap.Node matchesAlternative(Environment innerCaptures, Consumable value, Map.Entry<EdgePattern, RuleMap.Node> edge) {
                        value.mark();
                        innerCaptures.mark();
                        RuleMap.Node n = edge.getKey().matches(edge.getValue(), value, innerCaptures);
                        if(n != null) {
                            if(value.atEnd()) {
                                innerCaptures.commit();
                                value.commit();
                                return n;
                            }

                            for(Map.Entry<EdgePattern, RuleMap.Node> e: n.edges()) {
                                RuleMap.Node an = matchesAlternative(innerCaptures, value, e);
                                if(an != null) {
                                    innerCaptures.commit();
                                    value.commit();
                                    return an;
                                }
                            }
                        }

                        value.rollback();
                        innerCaptures.rollback();

                        return null;
                    }

                    @Override
                    public String toString() {
                        return "conforms to ";
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return getClass().isInstance(obj);
                    }
                });

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
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        Object theValue = value;
                        if(value instanceof Consumable)
                            theValue = ((Consumable)value).peek();

                        RuleMap.Node n = pseudoNode.match(theValue, captures);

                        if(n == endNode) {

                            if (value instanceof Consumable) {
                                captures.captureSingle(theValue);
                                ((Consumable) value).consume();
                            }

                            return target;
                        }

                        return null;
                    }
                });
            }
        }

        return new SubsumesList();
    }

    public static Pattern is(Class<?> type) {
        if(type == null)
            new String();

        class IsPattern implements Pattern {
            Pattern self = this;

            @Override
            public int compareInstanceTo(Pattern other) {
                return 0;
            }

            @Override
            public int sortIndex() {
                return 3;
            }

            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                boolean result = matchesSingle(value.peek(), captures);
                if(result) {
                    captures.captureSingle(value.peek());
                    value.consume();
                }
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                if(type.isInstance(value)) {
                    return true;
                }

                return false;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                class IsEdgePattern implements EdgePattern {
                    Class<?> theType = type;

                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        if(value instanceof Consumable)
                            return matchesList(((Consumable) value), captures) ? target : null;

                        RuleMap.Node n = matchesSingle(value, captures) ? target : null;
                        if(n != null)
                            captures.captureSingle(value);
                        return n;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof IsEdgePattern && theType.equals(((IsEdgePattern)obj).theType);
                    }

                    @Override
                    public String toString() {
                        return "is " + type;
                    }
                }

                return node.byPattern(new IsEdgePattern());
            }
        }

        return new IsPattern();
    }

    public static Pattern subsumesToMap(List<Map.Entry<String, Pattern>> map) {
        class SubsumesMap implements Pattern {
            List<Map.Entry<String, Pattern>> theMap = map;

            private boolean isMoreGeneral(List<Map.Entry<String, Pattern>> moreGeneralTest, List<Map.Entry<String, Pattern>> lessGeneralTest) {
                return lessGeneralTest.stream().allMatch(lessGeneralEntry -> {
                    Optional<Map.Entry<String, Pattern>> moreGeneralEntry = moreGeneralTest.stream().filter(y -> y.getKey().equals(lessGeneralEntry.getKey())).findFirst();

                    return !moreGeneralEntry.isPresent() || lessGeneralEntry.getValue().compareTo(moreGeneralEntry.get().getValue()) <= 0;
                }) && lessGeneralTest.size() >= moreGeneralTest.size();
            }

            @Override
            public int compareInstanceTo(Pattern other) {
                if(isMoreGeneral(theMap, ((SubsumesMap)other).theMap))
                    return 1;
                else if(isMoreGeneral(((SubsumesMap)other).theMap, theMap))
                    return -1;

                return 0;
            }

            @Override
            public int sortIndex() {
                return 2;
            }

            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                boolean result = matchesSingle(value.peek(), captures);
                value.consume();
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                if(value instanceof Map) {
                    Map<String, Object> otherMap = (Map<String, Object>) value;
                    return map.stream()
                        .allMatch(e -> {
                            Object slotValue = otherMap.get(e.getKey());
                            return slotValue != null && e.getValue().matchesSingle(slotValue, captures);
                        });
                }

                return false;
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
                        return SubsumesMap.this;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        Consumable consumable = (Consumable)value;

                        if(consumable.peek() instanceof Map) {
                            Map<String, Object> otherMap = (Map<String, Object>) consumable.peek();
                            return map.stream()
                                .allMatch(e -> {
                                    Object slotValue = otherMap.get(e.getKey());

                                    if(slotValue != null) {
                                        // Wrap into Consumable
                                        slotValue = new ListConsumable(Arrays.asList(slotValue));
                                        RuleMap.Node node = nodes.get(e.getKey());
                                        return node.match(slotValue, captures) != null;
                                    }
                                    return false;
                                }) ? target : null;
                        }

                        return null;
                    }
                }

                return node.byPattern(new SubsumesMapEdgePattern());
            }
        }

        return new SubsumesMap();
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
                return this.thePattern.compareInstanceTo(((CapturePattern)other).thePattern);
            }

            @Override
            public int sortIndex() {
                return pattern.sortIndex();
            }

            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                return false;
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                return false;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                RuleMap.Node pseudoNode = new RuleMap.Node();
                RuleMap.Node endNode = thePattern.findNode(pseudoNode);

                class CaptureEdgePattern implements EdgePattern {
                    @Override
                    public Pattern pattern() {
                        return CapturePattern.this;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        /*
                        Changed captures parameter into List in which, arguments are bound for actions
                        Don't use environment for capturing; decorate consumable and put the consumed
                        values into captures List at the given index.
                        - Could be prettyfied
                        */

                        Consumable consumable = (Consumable) value;

                        CaptureConsumable captureConsumable = new CaptureConsumable(consumable);

                        Consumable c = new ObservedConsumable(captureConsumable, v ->
                            consumable.propogate(v));

                        RuleMap.Node n = pseudoNode.match(c, captures);

                        if(n == endNode) {
                            Object valueToCapture = valueExtractor.apply(captureConsumable.getCapturedElements());
                            captures.captureSingle(index, valueToCapture);

                            return target;
                        }

                        return null;
                    }
                }

                return node.byPattern(new CaptureEdgePattern());
            }
        }

        return new CapturePattern();
    }

    private static class AnyPattern implements Pattern {
        Pattern self = this;

        @Override
        public int compareInstanceTo(Pattern other) {
            return 0;
        }

        @Override
        public int sortIndex() {
            return 6;
        }

        @Override
        public boolean matchesList(Consumable value, Environment captures) {
            value.consume();
            return true;
        }

        @Override
        public boolean matchesSingle(Object value, Environment captures) {
            return true;
        }

        @Override
        public RuleMap.Node findNode(RuleMap.Node node) {
            class AnythingEdgePattern implements EdgePattern {
                @Override
                public Pattern pattern() {
                    return self;
                }

                @Override
                public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                    if(value instanceof Consumable) {
                        Object val = ((Consumable)value).peek();
                        ((Consumable)value).consume();
                        captures.captureSingle(val);
                    } else
                        captures.captureSingle(value);

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
                return 5;
            }

            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                // Captured elements should implicitly be put into lists

                while(!value.atEnd() && pattern.matchesList(value, captures));

                return true;
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                return false;
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
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        Environment innerCaptures = new Environment();

                        while(!((Consumable)value).atEnd()) {
                            RuleMap.Node n = pseudoNode.match(value, innerCaptures);
                            if(n != patternNode)
                                break;
                        }

                        captures.captureSingle(innerCaptures.toList());

                        return target;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof RepeatEdgePattern && pseudoNode.equals(((RepeatEdgePattern)obj).pseudoNode);
                    }

                    @Override
                    public String toString() {
                        return pattern + " ...";
                    }
                }

                RuleMap.Node repeatNode = node.byPattern(new RepeatEdgePattern());

                return repeatNode;
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
                return 4;
            }

            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                return false;
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                return false;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                RuleMap.Node pseudoNode = new RuleMap.Node();
                RuleMap.Node patternToNegateNode = patternToNegate.findNode(pseudoNode);

                class NotEdgePattern implements EdgePattern {
                    Pattern thePatternToNegate = patternToNegate;

                    @Override
                    public Pattern pattern() {
                        return self;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        if(value instanceof Consumable)
                            ((Consumable)value).mark();

                        RuleMap.Node n = pseudoNode.match(value, captures);

                        if(value instanceof Consumable)
                            ((Consumable)value).rollback();

                        if(n == null) {
                            if(value instanceof Consumable) {
                                // What if embedded pattern is repeat pattern? Should that be possible?
                                // Or only single-capturing patterns?
                                captures.captureSingle(((Consumable)value).peek());
                                ((Consumable)value).consume();
                            }

                            return target;
                        }
                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof NotEdgePattern && this.thePatternToNegate.equals(((NotEdgePattern)obj).thePatternToNegate);
                    }
                }

                return node.byPattern(new NotEdgePattern());
            }
        }

        return new NotPattern();
    }
}
