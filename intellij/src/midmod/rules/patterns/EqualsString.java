package midmod.rules.patterns;

import midmod.pal.Consumable;

import java.util.Map;

public class EqualsString implements Pattern {
    private String value;

    public EqualsString(String value) {
        this.value = value;
    }

    @Override
    public boolean matches(Consumable value, Map<String, Object> captures) {
        return this.value.equals(value);
    }
}
