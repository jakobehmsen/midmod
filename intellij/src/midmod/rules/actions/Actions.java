package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Arrays;
import java.util.function.BiFunction;

public class Actions {
    public static <T, R> Action binary(BiFunction<T, R, Object> function) {
        return new Action() {
            @Override
            public Object perform(RuleMap ruleMap, Environment captures) {
                return function.apply((T)captures.get(0), (R)captures.get(1));
            }

            @Override
            public Object toValue() {
                // TODO: How to convert function?
                return Arrays.asList("binary", function);
            }
        };

        //return (ruleMap, captures) -> function.apply((T)captures.getIndexFor(0), (R)captures.getIndexFor(1));
    }
}
