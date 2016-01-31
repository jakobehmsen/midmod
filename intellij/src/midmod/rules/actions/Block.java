package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.patterns.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Block implements Action {
    private List<Action> actions;

    public Block(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
8        Object res = null;

        for (Action action : actions)
            res = action.perform(ruleMap, local, captures);

        return res;
    }

    @Override
    public Object toValue() {
        ArrayList<Object> value = new ArrayList<>();
        value.add("block");
        List<Object> values = actions.stream().map(x -> x.toValue()).collect(Collectors.toList());
        value.addAll(values);
        return value;
    }

    public static void defineParseRule(RuleMap ruleMap) {
        ruleMap.define(Patterns.captureSingle(0, Patterns.subsumesList(
            Patterns.equalsObject("block"),
            Patterns.repeat(Patterns.anything)
        )), (ruleMap1, local, captures) -> {
            List<Object> values = (List<Object>) captures.get(0);
            List<Action> actions = values
                .subList(1, values.size()).stream()
                .map(x ->
                    (Action) Match.on(ruleMap, ruleMap, x)).collect(Collectors.toList());
            return new Block(actions);
        });
    }
}
