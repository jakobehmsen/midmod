package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NewMap implements Action {
    private List<Map.Entry<String, Action>> slots;

    public NewMap(List<Map.Entry<String, Action>> slots) {
        this.slots = slots;
    }

    @Override
    public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
        return slots.stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().perform(ruleMap, local, captures)));
    }

    @Override
    public Object toValue() {
        return Arrays.asList("new-map", slots.stream().map(x -> Arrays.asList(x.getKey(), x.getValue().toValue())).collect(Collectors.toList()));
    }
}
