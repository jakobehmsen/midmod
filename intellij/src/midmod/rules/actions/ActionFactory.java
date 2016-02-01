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

    public static List<Object> localRules() {
        return Arrays.asList("local-rules");
    }

    public static List<Object> define(List<Object> target, List<Object> patternExpression, List<Object> actionExpression) {
        return Arrays.asList("define", target, patternExpression, actionExpression);
    }

    public static List<Object> block(List<Object>... actionValues) {
        return block(Arrays.asList(actionValues));
    }

    public static List<Object> block(List<Object> actionValues) {
        return Arrays.asList("block", actionValues);
    }

    public static List<Object> match(List<Object> valueExpression) {
        return match(globalRules(), localRules(), valueExpression);
    }

    public static List<Object> match(List<Object> ruleMapExpression, List<Object> localExpression, List<Object> valueExpression) {
        return Arrays.asList("match", ruleMapExpression, localExpression, valueExpression);
    }

    public static List<Object> list(Object... actionValues) {
        return Arrays.asList("list", Arrays.asList(actionValues));
    }
}
