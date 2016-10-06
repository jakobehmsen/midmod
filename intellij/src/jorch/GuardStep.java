package jorch;

import java.util.Map;
import java.util.function.Predicate;

public class GuardStep implements Step {
    private Predicate<Map<String, Object>> predicate;
    private Step ifTrue;
    private Step ifFalse;

    public GuardStep(Predicate<Map<String, Object>> predicate, Step ifTrue, Step ifFalse) {
        this.predicate = predicate;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public void perform(Token token, Map<String, Object> context) {
        if(predicate.test(context))
            token.perform(context, ifTrue, (t, ctx) -> t.moveNext());
        else
            token.perform(context, ifFalse, (t, ctx) -> t.moveNext());
    }
}
