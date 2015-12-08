package midmod.rules.actions;

import midmod.rules.RuleMap;

import java.util.List;
import java.util.Map;

public class Block implements Action {
    private List<Action> actions;

    public Block(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public Object perform(RuleMap ruleMap, Map<String, Object> captures) {
        Object res = null;

        for (Action action : actions)
            res = action.perform(ruleMap, captures);

        return res;
    }
}
