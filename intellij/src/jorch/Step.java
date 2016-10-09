package jorch;

import java.util.Arrays;
import java.util.Map;

public interface Step {
    void perform(Token token, Map<String, Object> context);
    default SequentialStep then(Step next) {
        return new SequentialStep(Arrays.asList(this, next));
    }
}
