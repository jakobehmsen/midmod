package midmod.rules;

import java.util.Map;

public interface EdgePattern {
    int sortIndex();

    RuleMap.Node matches(RuleMap.Node node, Object value, Environment captures);
}
