package jorch;

import java.util.Map;
import java.util.function.Predicate;

public class GuardStep implements Step {
    private Predicate<Token> predicate;
    private Step ifTrue;
    private Step ifFalse;

    public GuardStep(Predicate<Token> predicate, Step ifTrue, Step ifFalse) {
        this.predicate = predicate;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public void perform(Token token) {
        if(predicate.test(token))
            token.perform(ifTrue, (t) -> t.moveNext());
        else
            token.perform(ifFalse, (t) -> t.moveNext());
    }
}
