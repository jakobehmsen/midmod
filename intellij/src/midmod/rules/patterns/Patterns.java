package midmod.rules.patterns;

import midmod.pal.CaptureConsumable;
import midmod.pal.Consumable;
import midmod.pal.ListConsumable;
import midmod.rules.RuleMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Patterns {
    public static Pattern capture(Pattern pattern, String name) {
        return capture(pattern, name, true);
    }

    public static Pattern capture(Pattern pattern, String name, boolean isSingle) {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
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
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                boolean result = pattern.matchesSingle(value, captures);

                if(result) {
                    Object capture = value;
                    captures.put(name, capture);
                }

                return result;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                return null;
            }
        };
    }

    public static Pattern equalsObject(Object obj) {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                boolean result = matchesSingle(value.peek(), captures);
                value.consume();
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                if(obj.equals("replace"))
                    new String();

                return value.equals(obj);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                return node.byEquals(obj);
            }
        };
    }

    public static Pattern binary(String operator, Class<?> lhsType, Class<?> rhsType) {
        return Patterns.conformsTo(
            Patterns.equalsObject(operator),
            Patterns.capture(Patterns.is(lhsType), "lhs"),
            Patterns.capture(Patterns.is(rhsType), "rhs")
        );
    }

    public static Pattern conformsTo(Pattern... items) {
        return conformsTo(Arrays.asList(items));
    }

    public static Pattern conformsTo(List<Pattern> list) {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                boolean result = matchesSingle(value.peek(), captures);
                value.consume();
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
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
                return null;
            }
        };
    }

    public static Pattern is(Class<?> type) {
        if(type == null)
            new String();

        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                boolean result = matchesSingle(value.peek(), captures);
                value.consume();
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                return type.isInstance(value);
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                return node.byType(type);
            }
        };
    }

    public static Pattern conformsToMap(List<Map.Entry<String, Pattern>> map) {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                boolean result = matchesSingle(value.peek(), captures);
                value.consume();
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
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
        public boolean matchesList(Consumable value, Map<String, Object> captures) {
            value.consume();
            return true;
        }

        @Override
        public boolean matchesSingle(Object value, Map<String, Object> captures) {
            return true;
        }

        @Override
        public RuleMap.Node findNode(RuleMap.Node node) {
            return null;
        }
    };

    public static Pattern repeat(Pattern pattern) {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                // Captured elements should implicitly be put into lists

                while(!value.atEnd() && pattern.matchesList(value, captures));

                return true;
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                return false;
            }

            @Override
            public RuleMap.Node findNode(RuleMap.Node node) {
                return null;
            }
        };
    }
}
