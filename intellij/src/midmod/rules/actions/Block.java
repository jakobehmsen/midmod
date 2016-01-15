package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Block implements Action {
    private List<Action> actions;

    public Block(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public Object perform(RuleMap ruleMap, Environment captures) {
        Object res = null;

        for (Action action : actions)
            res = action.perform(ruleMap, captures);

        return res;
    }

    @Override
    public Object toValue() {
        return Arrays.asList("block", actions.stream().map(x -> x.toValue()).collect(Collectors.toList()));
    }
}
