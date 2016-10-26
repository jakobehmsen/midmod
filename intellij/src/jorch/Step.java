package jorch;

import java.io.Serializable;
import java.util.Arrays;

public interface Step extends Serializable {
    void perform(Token token);
    default SequentialStep then(Step next) {
        return new SequentialStep(Arrays.asList(this, next));
    }
}
