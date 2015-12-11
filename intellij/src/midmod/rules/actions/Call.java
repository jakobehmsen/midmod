package midmod.rules.actions;

import midmod.rules.RuleMap;

import java.util.Hashtable;
import java.util.Map;

public class Call implements Action {
    private Action action;

    public Call(Action action) {
        this.action = action;
    }

    @Override
    public Object perform(RuleMap ruleMap, Map<String, Object> captures) {
        Object value = action.perform(ruleMap, captures);

        return on(ruleMap, value);
    }

    public static Object on(RuleMap ruleMap, Object value) {
        Hashtable<String, Object> resolvedCaptures = new Hashtable<>();
        Action resolvedAction = ruleMap.resolve(value, resolvedCaptures);

        return resolvedAction.perform(ruleMap, resolvedCaptures);
    }
}
