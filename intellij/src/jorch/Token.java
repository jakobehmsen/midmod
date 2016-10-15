package jorch;

import java.util.Map;

public interface Token {
    void perform(Map<String, Object> context, Step step, Step callback);
    void moveNext();
    Step currentStep();
}
