package midmod.rules.patterns;

import java.util.List;
import java.util.stream.IntStream;

public class Patterns {
    public static Pattern capture(String name) {
        return (value, captures) -> {
            captures.put(name, value);

            return true;
        };
    }

    public static Pattern isInteger() {
        return (value, captures) -> value instanceof Integer;
    }

    public static Pattern isString() {
        return (value, captures) -> value instanceof String;
    }

    public static Pattern equalsString(String str) {
        return (value, captures) -> str.equals(value);
    }

    public static Pattern conformsTo(List<Pattern> list) {
        return (value, captures) -> {
            if(value instanceof List) {
                List<Object> otherList = (List<Object>) value;
                return IntStream.range(0, list.size())
                    .allMatch(i -> list.get(i).matches(otherList.get(i), captures));
            }

            return false;
        };
    }
}
