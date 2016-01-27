package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Arrays;

public class LoadAsConstant implements Action {
    private int index;

    public LoadAsConstant(int index) {
        this.index = index;
    }

    @Override
    public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
        return new Constant(captures.get(index));
    }

    @Override
    public Object toValue() {
        return Arrays.asList("load-as-constant", index);
    }
}
