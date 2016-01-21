package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.ValueConvertible;

import java.util.Map;

public interface Action extends ValueConvertible {
    Object perform(RuleMap ruleMap, RuleMap local, Environment captures);
    default Object toValue() {
        return this;
    }
}
