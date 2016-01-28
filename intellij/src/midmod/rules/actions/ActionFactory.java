package midmod.rules.actions;

import java.util.Arrays;
import java.util.List;

public class ActionFactory {
    public static List<Object> constant(Object obj) {
        return Arrays.asList("constant", obj);
    }
}
