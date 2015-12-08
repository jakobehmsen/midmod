package midmod.rules.patterns;

import java.util.Map;

public class EqualsString implements Pattern {
    private String value;

    public EqualsString(String value) {
        this.value = value;
    }

    @Override
    public boolean matches(Object value, Map<String, Object> captures) {
        return this.value.equals(value);
    }
}
