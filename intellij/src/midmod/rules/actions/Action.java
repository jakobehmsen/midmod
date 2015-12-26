package midmod.rules.actions;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Map;

public interface Action {
    Object perform(RuleMap ruleMap, Environment captures);
}
