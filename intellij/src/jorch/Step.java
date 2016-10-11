package jorch;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public interface Step extends Serializable {
    void perform(Token token, Map<String, Object> context);
    default SequentialStep then(Step next) {
        return new SequentialStep(Arrays.asList(this, next));
    }
}
