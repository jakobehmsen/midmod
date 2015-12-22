package midmod.rules.patterns;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Patterns {
    public static Pattern capture(String name) {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                return matchesSingle(value, captures);
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                captures.put(name, value);

                return true;
            }
        };

        /*return (value, captures) -> {
            captures.put(name, value);

            return true;
        };*/
    }

    public static Pattern isInteger() {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                boolean result = matchesSingle(value.peek(), captures);
                value.consume();
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                return value instanceof Integer;
            }
        };

        //return (value, captures) -> value.peek() instanceof Integer;
    }

    public static Pattern isString() {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                boolean result = matchesSingle(value.peek(), captures);
                value.consume();
                return result;
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                return value instanceof String;
            }
        };

        /*return (value, captures) -> {
            boolean result = value.peek() instanceof String;
            value.consume();
            return result;
        };*/
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
                return value.equals(obj);
            }
        };

        /*return (value, captures) -> {
            boolean result = value.peek().equals(obj);
            value.consume();
            return result;
        };*/
    }

    public static Pattern binary(String operator, Class<?> lhsType, Class<?> rhsType) {
        return Patterns.conformsTo(
            Patterns.equalsObject(operator),
            Patterns.is(lhsType).andThen(Patterns.capture("lhs")),
            Patterns.is(rhsType).andThen(Patterns.capture("rhs"))
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
                    Consumable listConsumable = Consumable.Util.wrap(otherList);
                    return IntStream.range(0, list.size())
                       .allMatch(i ->
                           list.get(i).matchesList(listConsumable, captures));
                }

                return false;
            }
        };

        /*return (value, captures) -> {
            // Should check for special ListConsumable? Or just not singleton consumable?
            if(value instanceof ListConsumable) {
                //List<Object> otherList = (List<Object>) value;
                //return IntStream.range(0, list.size())
                //    .allMatch(i -> list.get(i).matchesList(Consumable.Util.wrap(otherList.get(i)), captures));
                // Cast to special ListConsumable?
                ListConsumable consumableList = (ListConsumable) value;
                return
                    list.stream().allMatch(x -> x.matchesList(consumableList, captures)) &&
                    consumableList.atEnd();
            }

            return false;
        };*/
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
        };

        /*return (value, captures) -> {
            boolean result = type.isInstance(value.peek());;
            value.consume();
            return result;
        };*/
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
        };

        /*return (value, captures) -> {
            if(value instanceof Map) {
                Map<String, Object> otherMap = (Map<String, Object>) value;
                return map.stream()
                    .allMatch(e -> {
                        Object slotValue = otherMap.get(e.getKey());
                        return slotValue != null && e.getValue().matchesList(Consumable.Util.wrap(slotValue), captures);
                    });
            }

            return false;
        };*/
    }

    public static final Pattern anything = new Pattern() {
        @Override
        public boolean matchesList(Consumable value, Map<String, Object> captures) {
            return true;
        }

        @Override
        public boolean matchesSingle(Object value, Map<String, Object> captures) {
            return true;
        }
    };

    public static Pattern repeat(Pattern pattern) {
        return new Pattern() {
            @Override
            public boolean matchesList(Consumable value, Map<String, Object> captures) {
                // Captured elements should implicitly be put into lists

                while(!value.atEnd() && pattern.matchesList(value, captures));
                //value.consume();

                return true;
            }

            @Override
            public boolean matchesSingle(Object value, Map<String, Object> captures) {
                return false;
            }
        };

        /*return (value, captures) -> {
            // Captured elements should implicitly be put into lists

            while(!value.atEnd() && pattern.matchesList(value, captures));
                //value.consume();

            return true;
        };*/
    }
}
