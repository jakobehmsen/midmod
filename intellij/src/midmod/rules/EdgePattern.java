package midmod.rules;

import midmod.rules.patterns.Pattern;

import java.util.Map;

public interface EdgePattern {
    Pattern pattern();

    RuleMap.Node matches(RuleMap.Node target, Object value, Environment captures);
}
