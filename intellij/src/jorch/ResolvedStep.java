package jorch;

import java.util.Map;
import java.util.function.Function;

public class ResolvedStep implements Step {
    private Function<Map<String, Object>, Step> resolver;

    public ResolvedStep(Function<Map<String, Object>, Step> resolver) {
        this.resolver = resolver;
    }

    @Override
    public void perform(Token token, Map<String, Object> context) {
        Step step = resolver.apply(context);
        token.perform(context, step, (t, ctx) -> t.moveNext());
    }
}
