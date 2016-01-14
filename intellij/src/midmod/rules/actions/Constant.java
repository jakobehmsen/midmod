package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.ValueConvertible;

public class Constant implements Action {
    private Object value;

    public Constant(Object value) {
        this.value = value;
    }

    @Override
    public Object perform(RuleMap ruleMap, Environment captures) {
        //return value;
        return value instanceof ValueConvertible ? ((ValueConvertible)value).toValue() : value;
    }
}
