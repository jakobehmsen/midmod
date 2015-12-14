package midmod.rules;

import midmod.rules.actions.Action;
import midmod.rules.patterns.Pattern;

import java.util.LinkedHashMap;
import java.util.Map;

public class RuleMap {
    private LinkedHashMap<Pattern, Action> rules = new LinkedHashMap<>();

    public void define(Pattern pattern, Action action) {
        rules.put(pattern, action);
    }

    public Action resolve(Object value, Map<String, Object> captures) {
        //System.out.println(value);

        return rules.entrySet().stream().filter(x -> x.getKey().matches(value, captures)).findFirst().get().getValue();
    }
}
