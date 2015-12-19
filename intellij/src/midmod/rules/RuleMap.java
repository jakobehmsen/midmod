package midmod.rules;

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

        if(!rules.entrySet().stream().anyMatch(x -> x.getKey().matches(value, captures)))
            new String();

        return rules.entrySet().stream().filter(x -> x.getKey().matches(value, captures)).findFirst().get().getValue();
    }
}
