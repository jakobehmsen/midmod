package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Arrays;

public class Match implements Action {
    private Action action;

    public Match(Action action) {
        this.action = action;
    }

    @Override
    public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
        Object value = action.perform(ruleMap, local, captures);

        return on(ruleMap, local, value);
    }

    public static Object on(RuleMap ruleMap, RuleMap local, Object value) {
        //Hashtable<String, Object> resolvedCaptures = new Hashtable<>();
        Environment resolvedCaptures = new Environment();
        Action resolvedAction = ruleMap.resolve(value, resolvedCaptures, local);

        return resolvedAction.perform(ruleMap, local, resolvedCaptures);
    }

    @Override
    public Object toValue() {
        return Arrays.asList("match", action.toValue());
    }
}
