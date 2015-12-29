package midmod.rules.patterns;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;
import midmod.rules.EdgePattern;
import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Patterns {
    /*public static Pattern capture(Pattern pattern, String name) {
        return capture(pattern, name, true);
    }*/

    /*public static Pattern capture(Pattern pattern, String name, boolean isSingle) {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Environment captures) {
                CaptureConsumable captureConsumable = new CaptureConsumable(value);

                boolean result = pattern.matchesList(captureConsumable, captures);

                if(result) {
                    Object capture = isSingle
                        ? captureConsumable.getCapturedElements().get(0)
                        : captureConsumable.getCapturedElements();
                    captures.put(name, capture);
                }

                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Environment captures) {
                boolean result = pattern.matchesSingle(value, captures);

                if(result) {
                    Object capture = value;
                    captures.put(name, capture);
                }

                return result;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                // sortIndex should be part of the atomic pattern that is added to the node



                class CaptureEdgePattern implements EdgePattern {
                    public EdgePattern edgePattern;

                    @Override
                    public int sortIndex() {
                        return edgePattern.sortIndex();
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node node, Object value, Environment captures) {
                        if (value instanceof Consumable) {
                            CaptureConsumable captureConsumable = new CaptureConsumable((Consumable) value);

                            RuleMap.Node n = node.match(captureConsumable, captures);

                            if (n != null) {
                                Object capture = isSingle
                                    ? captureConsumable.getCapturedElements().get(0)
                                    : captureConsumable.getCapturedElements();
                                captures.put(name, capture);
                            }

                            return n;
                        }

                        RuleMap.Node n = node.match(value, captures);

                        if (n != null) {
                            Object capture = value;
                            captures.put(name, capture);
                        }

                        return n;
                    }

                    @Override
                    public String toString() {
                        return "capture " + name;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return obj instanceof CaptureEdgePattern;
                    }
                }

                //RuleMap.Node captureNode = new RuleMap.Node();


                CaptureEdgePattern captureEdgePattern = new CaptureEdgePattern();
                RuleMap.Node captureNode = node.byPattern(captureEdgePattern);
                RuleMap.Node patternNode = pattern.findNode(captureNode);
                EdgePattern edgePattern = captureNode.getEdge(patternNode);
                captureEdgePattern.edgePattern = edgePattern;

                return captureNode;
            }
        };
    }*/

    public static Pattern equalsObject(Object obj) {
        return new Pattern() {
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
                //return node.byEquals(obj);

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
                    public int sortIndex() {
                        return 0;
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
        };
    }

    public static Pattern binary(String operator, Class<?> lhsType, Class<?> rhsType) {
        return Patterns.conformsTo(
            Patterns.equalsObject(operator),
            Patterns.is(lhsType),
            Patterns.is(rhsType)
            /*Patterns.capture(Patterns.is(lhsType), "lhs"),
            Patterns.capture(Patterns.is(rhsType), "rhs")*/
        );
    }

    public static Pattern conformsTo(Pattern... items) {
        return conformsTo(Arrays.asList(items));
    }

    public static Pattern conformsTo(List<Pattern> list) {
        return new Pattern() {
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
                    public int sortIndex() {
                        return 0;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        if (value instanceof List) {
                            List<Object> otherList = (List<Object>) value;
                            Consumable listConsumable = new ListConsumable(otherList);

                            for(Map.Entry<EdgePattern, RuleMap.Node> e: target.edges()) {
                                //captures.startCompositeCapture();
                                //Environment innerCaptures = captures.getCurrent();
                                Environment innerCaptures = new Environment();
                                RuleMap.Node an = matchesAlternative(innerCaptures, listConsumable, e);
                                if(an != null && listConsumable.atEnd()) {
                                    captures.captureSingle(innerCaptures.toList());
                                    //captures.endCompositeCapture();
                                    return an;
                                }
                            }

                            /*captures.startCompositeCapture();
                            Environment innerCaptures = captures.getCurrent();
                            //Consumable listConsumable = new ListConsumable(otherList);
                            RuleMap.Node n = target;
                            while (true) {
                                n = n.match(listConsumable, innerCaptures);
                                if (listConsumable.atEnd()) {
                                    captures.endCompositeCapture();
                                    return n;
                                }
                                if (n == null)
                                    return null;
                            }*/
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
                    n = pattern.findNode(n);

                /*node.putPattern(new EdgePattern() {
                    @Override
                    public int sortIndex() {
                        return 0;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Map<String, Object> captures) {
                        if(value instanceof List) {
                            List<Object> otherList = (List<Object>) value;
                            Consumable listConsumable = new ListConsumable(otherList);
                            RuleMap.Node n;
                            while(true) {
                                n = target.match(listConsumable, captures);
                                if(listConsumable.atEnd())
                                    return n;
                                if(n == null)
                                    return null;
                            }
                        }

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
                }, listNode);*/



                /*RuleMap.Node bySequence = node.bySequence();

                RuleMap.Node n = bySequence;

                for (Pattern pattern : list)
                    n = pattern.findNode(n);*/

                return n;
            }
        };
    }

    public static Pattern is(Class<?> type) {
        if(type == null)
            new String();

        return new Pattern() {
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
                    public int sortIndex() {
                        return 1;
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

                //return node.byType(type);
                return node.byPattern(new IsEdgePattern());
            }
        };
    }

    public static Pattern conformsToMap(List<Map.Entry<String, Pattern>> map) {
        return new Pattern() {
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
                return null;
            }
        };
    }

    public static final Pattern anything = new Pattern() {
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
                public int sortIndex() {
                    return 3;
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
    };

    public static Pattern repeat(Pattern pattern) {
        return new Pattern() {
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
                    public int sortIndex() {
                        return 2;
                    }

                    @Override
                    public RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures) {
                        //captures.startCompositeCapture();
                        //Environment innerCaptures = captures.getCurrent();
                        Environment innerCaptures = new Environment();

                        while(!((Consumable)value).atEnd()) {
                            RuleMap.Node n = pseudoNode.match(value, innerCaptures);
                            if(n != patternNode)
                                break;
                        }

                        //captures.endCompositeCapture();
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
        };
    }
}
