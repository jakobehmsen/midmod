package midmod.rules;

import midmod.rules.actions.Action;
import midmod.rules.patterns.Pattern;

public class Rule {
    private Pattern pattern;
    private Action action;

    public Rule(Pattern pattern, Action action) {
        this.pattern = pattern;
        this.action = action;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return pattern + " => " + action;
    }
}
