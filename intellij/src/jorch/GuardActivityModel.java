package jorch;

import java.util.Map;
import java.util.function.Predicate;

public class GuardActivityModel implements ActivityModel {
    private Predicate<Map<String, Object>> predicate;
    private Step ifTrue;
    private Step ifFalse;

    public GuardActivityModel(Predicate<Map<String, Object>> predicate, Step ifTrue, Step ifFalse) {
        this.predicate = predicate;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public Step toStep() {
        return new GuardStep(predicate, ifTrue, ifFalse);
    }

    @Override
    public String toString() {
        return "if(" + predicate.toString() + ") {" + ifTrue + "} else {" + ifFalse + "}";
    }
}
