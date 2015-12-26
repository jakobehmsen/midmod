package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

public class Constant implements Action {
    private Object value;

    public Constant(Object value) {
        this.value = value;
    }

    @Override
    public Object perform(RuleMap ruleMap, Environment captures) {
        return value;
    }
}
