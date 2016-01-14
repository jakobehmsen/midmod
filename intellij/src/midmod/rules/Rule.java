package midmod.rules;

import midmod.rules.actions.Action;
import midmod.rules.patterns.Pattern;

import java.util.Arrays;

public class Rule implements ValueConvertible {
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

    @Override
    public Object toValue() {
        //return null;
        // Pattern and Action must be ValueConvertibles as well
        return Arrays.asList(pattern, action);
    }
}
