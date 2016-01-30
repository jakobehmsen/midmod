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

    public static List<Object> subsumesList(List<Object>... items) {
        return Arrays.asList("subsumes-list", Arrays.asList(items));
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
}
