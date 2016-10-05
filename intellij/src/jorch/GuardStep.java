package jorch;

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
            token.moveInto(ifTrue, t -> t.moveOut());
        else
            token.moveInto(ifFalse, t -> t.moveOut());
    }
}
