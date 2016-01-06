package midmod.rules;

import midmod.pal.Consumable;
import midmod.rules.patterns.Pattern;

import java.util.Map;

public interface EdgePattern {
    Pattern pattern();

    RuleMap.Node matches(RuleMap.Node target, Consumable consumable, Environment captures);
}
