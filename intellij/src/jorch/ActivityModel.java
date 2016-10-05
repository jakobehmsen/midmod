package jorch;

import java.util.Arrays;

public interface ActivityModel {
    Step toStep();
    default SequentialActivityModel then(ActivityModel next) {
        return new SequentialActivityModel(Arrays.asList(this, next));
    }
}
