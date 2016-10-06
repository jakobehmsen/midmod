package jorch;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CallStep implements Step {
    private Function<Map<String, Object>, Map<String, Object>> callContextSupplier;
    private BiConsumer<Map<String, Object>, Map<String, Object>> callContextConsumer;
    private Step step;

    public CallStep(Function<Map<String, Object>, Map<String, Object>> callContextSupplier, BiConsumer<Map<String, Object>, Map<String, Object>> callContextConsumer, Step step) {
        this.callContextSupplier = callContextSupplier;
        this.callContextConsumer = callContextConsumer;
        this.step = step;
    }

    @Override
    public void perform(Token token, Map<String, Object> context) {
        Map<String, Object> inputContext = callContextSupplier.apply(context);
        token.perform(inputContext, step, (t, ctx) -> {
            callContextConsumer.accept(ctx, context);
            t.moveNext();
        });
    }
}
