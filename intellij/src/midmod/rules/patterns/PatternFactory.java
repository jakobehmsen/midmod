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
}
