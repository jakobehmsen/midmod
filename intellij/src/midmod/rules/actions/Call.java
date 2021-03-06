package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Arrays;

public class Call implements Action {
    private Action action;

    public Call(Action action) {
        this.action = action;
    }

    @Override
    public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
        return action.perform(ruleMap, local, captures);
    }

    public static Object on(RuleMap ruleMap, RuleMap local, Object value) {
        //Hashtable<String, Object> resolvedCaptures = new Hashtable<>();
        Environment resolvedCaptures = new Environment();
        Action resolvedAction = ruleMap.resolve(value, resolvedCaptures, local);

        return resolvedAction.perform(ruleMap, local, resolvedCaptures);
    }

    public static Object onLocal(RuleMap ruleMap, RuleMap local, Object value) {
        //Hashtable<String, Object> resolvedCaptures = new Hashtable<>();
        Environment resolvedCaptures = new Environment();
        Action resolvedAction = local.resolve(value, resolvedCaptures, local);

        return resolvedAction.perform(ruleMap, local, resolvedCaptures);
    }

    @Override
    public Object toValue() {
        return Arrays.asList("call", action.toValue());
    }
}
