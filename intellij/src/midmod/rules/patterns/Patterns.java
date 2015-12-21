package midmod.rules.patterns;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Patterns {
    public static Pattern capture(String name) {
        return (value, captures) -> {
            captures.put(name, value);

            return true;
        };
    }

    public static Pattern isInteger() {
        return (value, captures) -> value.peek() instanceof Integer;
    }

    public static Pattern isString() {
        return (value, captures) -> {
            boolean result = value.peek() instanceof String;
            value.consume();
            return result;
        };
    }

    public static Pattern equalsObject(Object obj) {
        return (value, captures) -> {
            boolean result = value.peek().equals(obj);
            value.consume();
            return result;
        };
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
        return (value, captures) -> {
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
        };
    }

    public static Pattern is(Class<?> type) {
        if(type == null)
            new String();

        return (value, captures) -> {
            boolean result = type.isInstance(value.peek());;
            value.consume();
            return result;
        };
    }

    public static Pattern conformsToMap(List<Map.Entry<String, Pattern>> map) {
        return (value, captures) -> {
            if(value instanceof Map) {
                Map<String, Object> otherMap = (Map<String, Object>) value;
                return map.stream()
                    .allMatch(e -> {
                        Object slotValue = otherMap.get(e.getKey());
                        return slotValue != null && e.getValue().matchesList(Consumable.Util.wrap(slotValue), captures);
                    });
            }

            return false;
        };
    }

    public static final Pattern anything = (value, captures) -> true;

    public static Pattern repeat(Pattern pattern) {
        return (value, captures) -> {
            // Captured elements should implicitly be put into lists

            while(!value.atEnd() && pattern.matchesList(value, captures));
                //value.consume();

            return true;
        };
    }
}
