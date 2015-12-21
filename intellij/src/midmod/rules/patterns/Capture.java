package midmod.rules.patterns;

import midmod.pal.Consumable;

import java.util.Map;

public class Capture implements Pattern {
    private String name;

    public Capture(String name) {
        this.name = name;
    }

    @Override
    public boolean matchesList(Consumable value, Map<String, Object> captures) {
        captures.put(name, value);

        return true;
    }
}
