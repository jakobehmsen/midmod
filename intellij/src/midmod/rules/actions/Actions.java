package midmod.rules.actions;

import java.util.function.BiFunction;

public class Actions {
    public static <T, R> Action binary(BiFunction<T, R, Object> function) {
        return (ruleMap, captures) -> function.apply((T)captures.get(0), (R)captures.get(1));
    }
}
