package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.patterns.Patterns;

import java.util.Arrays;

public class Constant implements Action {
    private Object value;

    public Constant(Object value) {
        this.value = value;
    }

    @Override
    public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
        return value;
        // Have a special constant that converts using ValueConvertible?
        //return value instanceof ValueConvertible ? ((ValueConvertible)value).toValue() : value;
    }

    @Override
    public Object toValue() {
        // Should value be checked as ValueConvertible?
        return Arrays.asList("constant", value);
    }

    public static void defineParseRule(RuleMap ruleMap) {
        ruleMap.define(Patterns.conformsTo(
            Patterns.equalsObject("constant"),
            Patterns.captureSingle(0, Patterns.anything)
        ), (ruleMap1, local, captures) -> new Constant(captures.get(0)));
    }
}
