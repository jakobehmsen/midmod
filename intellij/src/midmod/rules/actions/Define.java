package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.patterns.Pattern;

public class Define implements Action {
    private Action patternAction;
    private Action actionAction;

    public Define(Action patternAction, Action actionAction) {
        this.patternAction = patternAction;
        this.actionAction = actionAction;
    }

    @Override
    public Object perform(RuleMap ruleMap, Environment captures) {
        Pattern pattern = (Pattern)patternAction.perform(ruleMap, captures);
        Action action = (Action)actionAction.perform(ruleMap, captures);

        ruleMap.define(pattern, action);

        return action;
    }
}
