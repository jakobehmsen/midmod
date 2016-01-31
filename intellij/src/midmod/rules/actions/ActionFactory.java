package midmod.rules.actions;

import java.util.Arrays;
import java.util.List;

public class ActionFactory {
    public static List<Object> constant(Object obj) {
        return Arrays.asList("constant", obj);
    }

    public static List<Object> globalRules() {
        return Arrays.asList("global-rules");
    }

    public static List<Object> define(List<Object> target, List<Object> patternExpression, List<Object> actionExpression) {
        return Arrays.asList("define", target, patternExpression, actionExpression);
    }

    public static List<Object> block(List<Object>... actions) {
        return Arrays.asList("block", Arrays.asList(actions));
    }
}
