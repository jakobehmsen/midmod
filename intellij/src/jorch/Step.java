package jorch;

import java.util.Map;

public interface Step {
    void perform(Token token, Map<String, Object> context);
}
