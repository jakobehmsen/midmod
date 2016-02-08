package midmod.rules.patterns;

import java.util.Arrays;
import java.util.List;

public class PatternFactory {
    public static List<Object> rule(List<Object> pattern, List<Object> action) {
        return Arrays.asList(pattern, action);
    }

    public static List<Object> newRuleMap(List<Object>... patternActions) {
        return Arrays.asList("new-rule-map", Arrays.asList(patternActions));
    }

    public static List<Object> equalsObject(Object obj) {
        return Arrays.asList("equals", obj);
    }

    public static List<Object> subsumesList(List<Object> items) {
        return Arrays.asList("subsumes-list", items);
    }

    public static List<Object> subsumesList(List<Object>... items) {
        return subsumesList(Arrays.asList(items));
    }

    public static List<Object> is(Class<?> type) {
        return Arrays.asList("is", type);
    }

    public static List<Object> slotDefinition(String name, List<Object> pattern) {
        return Arrays.asList(name, pattern);
    }

    public static List<Object> subsumesMap(List<Object>... slotDefinitions) {
        return Arrays.asList("subsumes-map", Arrays.asList(slotDefinitions));
    }

    public static List<Object> captureSingle(int index, List<Object> pattern) {
        return Arrays.asList("capture-single", index, pattern);
    }

    public static List<Object> captureMany(int index, List<Object> pattern) {
        return Arrays.asList("capture-many", index, pattern);
    }

    public static List<Object> subsumesRuleMap(List<Object>... patterns) {
        return Arrays.asList("subsumes-rule-map", Arrays.asList(patterns));
    }

    public static List<Object> anything() {
        return Arrays.asList("anything");
    }

    public static List<Object> not(List<Object> pattern) {
        return Arrays.asList("not", pattern);
    }

    public static List<Object> repeat(List<Object> pattern) {
        return Arrays.asList("repeat", pattern);
    }

    public static List<Object> or(List<Object> lhs, List<Object> rhs) {
        return Arrays.asList("or", lhs, rhs);
    }
}
