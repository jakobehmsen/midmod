package midmod.rules;

import midmod.pal.Consumable;
import midmod.rules.actions.Action;
import midmod.rules.actions.Actions;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.Patterns;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class RuleMap {
    private LinkedHashMap<Pattern, Action> rules = new LinkedHashMap<>();

    public void define(Pattern pattern, Action action) {
        rules.put(pattern, action);
    }

    public <T, R> void defineBinary(String operator, Class<T> lhsType, Class<R> rhsType, BiFunction<T, R, Object> function) {
        rules.put(Patterns.binary(operator, lhsType, rhsType), Actions.binary(function));
    }

    public Action resolve(Object value, Map<String, Object> captures) {
        //System.out.println(value);

        if(!rules.entrySet().stream().anyMatch(x -> isMatch(value, captures, x.getKey())))
            new String();

        return rules.entrySet().stream().filter(x -> isMatch(value, captures, x.getKey())).findFirst().get().getValue();
    }

    private boolean isMatch(Object value, Map<String, Object> captures, Pattern x) {
        return x.matchesSingle(value, captures);
    }
}
