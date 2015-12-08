package midmod.rules.actions;

import midmod.rules.RuleMap;

import java.util.Map;

public class Constant implements Action {
    private Object value;

    public Constant(Object value) {
        this.value = value;
    }

    @Override
    public Object perform(RuleMap ruleMap, Map<String, Object> captures) {
        return value;
    }
}
