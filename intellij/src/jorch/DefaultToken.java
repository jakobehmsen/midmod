package jorch;

import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

public class DefaultToken implements Token, Serializable {
    private Stack<Frame> frames = new Stack<>();

    public DefaultToken(Map<String, Object> context, Step step) {
        perform(context, step, new Finish());
    }

    @Override
    public void perform(Map<String, Object> context, Step step, Step callback) {
        Frame frame = new Frame(context, step, callback);
        frames.push(frame);

        schedule(() ->
            frames.peek().step.perform(this, frame.context));
    }

    @Override
    public void moveNext() {
        schedule(() -> {
            Frame frame = frames.pop();
            frame.callback.perform(this, frame.context);
        });
    }

    private static class Frame implements Serializable {
        private Map<String, Object> context;
        private Step step;
        private Step callback;

        private Frame(Map<String, Object> context, Step step, Step callback) {
            this.context = context;
            this.step = step;
            this.callback = callback;
        }
    }

    private static class Finish implements Step, Serializable {
        @Override
        public void perform(Token token, Map<String, Object> context) {
            ((DefaultToken)token).finished();
        }
    }

    @Override
    public Step currentStep() {
        return frames.peek().step;
    }

    @Override
    public void halt() { }

    protected void finished() { }

    protected void schedule(Runnable runnable) { }

    @Override
    public void proceed() { }
}
