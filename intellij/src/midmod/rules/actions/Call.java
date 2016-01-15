package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.stream.Collectors;

public class Call implements Action {
    private Action action;

    public Call(Action action) {
        this.action = action;
    }

    @Override
    public Object perform(RuleMap ruleMap, Environment captures) {
        Object value = action.perform(ruleMap, captures);

        return on(ruleMap, value);
    }

    public static Object on(RuleMap ruleMap, Object value) {
        //Hashtable<String, Object> resolvedCaptures = new Hashtable<>();
        Environment resolvedCaptures = new Environment();
        Action resolvedAction = ruleMap.resolve(value, resolvedCaptures);

        return resolvedAction.perform(ruleMap, resolvedCaptures);
    }

    @Override
    public Object toValue() {
        return Arrays.asList("call", action.toValue());
    }
}
