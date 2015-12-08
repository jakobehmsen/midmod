package midmod.rules.patterns;

import java.util.Map;

public class Capture implements Pattern {
    private String name;

    public Capture(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Object value, Map<String, Object> captures) {
        captures.put(name, value);

        return true;
    }
}
