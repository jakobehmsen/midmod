package midmod.rules.actions;

import midmod.rules.RuleMap;

import java.util.Map;

public interface Action {
    Object perform(RuleMap ruleMap, Map<String, Object> captures);
}
