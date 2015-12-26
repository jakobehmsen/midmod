package midmod.rules.actions;

import java.util.function.BiFunction;

public class Actions {
    public static <T, R> Action binary(BiFunction<T, R, Object> function) {
        // Assumed first capture is operator
        return (ruleMap, captures) -> function.apply((T)captures.getByAddress(0, 1), (R)captures.getByAddress(0, 2));
    }
}
