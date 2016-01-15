package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.patterns.Pattern;

import java.util.Arrays;

public class Define implements Action {
    private Action patternAction;
    private Action actionAction;

    public Define(Action patternAction, Action actionAction) {
        this.patternAction = patternAction;
        this.actionAction = actionAction;
    }

    @Override
    public Object perform(RuleMap ruleMap, Environment captures) {
        // TODO: When constant action is performed, then value is implicitly converted
        // Either: create a new Define action (or change this) that requires direct
        // patterns and actions rather than evaluated actions, and/or add support
        // for mapping values back into patterns and actions.
        // The first option is the easiest one.
        Pattern pattern = (Pattern)patternAction.perform(ruleMap, captures);
        Action action = (Action)actionAction.perform(ruleMap, captures);

        ruleMap.define(pattern, action);

        return action;
    }

    @Override
    public Object toValue() {
        return Arrays.asList("define", patternAction.toValue(), actionAction.toValue());
    }
}
