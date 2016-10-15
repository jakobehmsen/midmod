package jorch;

import java.io.Serializable;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

public class DefaultToken implements Token, Serializable {
    private Stack<Frame> frames = new Stack<>();
    private Consumer<DefaultToken> nextPart;

    public DefaultToken(Map<String, Object> context, Step step2) {
        perform(context, step2, new Finish());
    }

    @Override
    public void perform(Map<String, Object> context, Step step, Step callback) {
        Frame frame = new Frame(context, step, callback);
        frames.push(frame);
        nextPart = new PerformTask(frame);
    }

    protected void performTask(Runnable taskPerformer) {
        taskPerformer.run();
    }

    @Override
    public void moveNext() {
        Frame frame = frames.pop();
        nextPart = new MoveNext(frame);
    }

    protected void moveNextTask(Runnable moveNextPerformer) {
        moveNextPerformer.run();
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

    public void proceed() {
        Consumer<DefaultToken> part = nextPart;
        nextPart = null;
        part.accept(this);
    }

    private static class PerformTask implements Consumer<DefaultToken>, Serializable {
        private final Frame frame;

        public PerformTask(Frame frame) {
            this.frame = frame;
        }

        @Override
        public void accept(DefaultToken defaultToken) {
            defaultToken.performTask(() -> {
                frame.step.perform(defaultToken, frame.context);
            });
        }
    }

    private static class MoveNext implements Consumer<DefaultToken>, Serializable {
        private final Frame frame;

        public MoveNext(Frame frame) {
            this.frame = frame;
        }

        @Override
        public void accept(DefaultToken defaultToken) {
            frame.callback.perform(defaultToken, frame.context);
        }
    }

    protected void finished() {

    }

    private static class Finish implements Step {
        @Override
        public void perform(Token token, Map<String, Object> context) {
            ((DefaultToken)token).finished();
        }
    }

    @Override
    public Step currentStep() {
        return frames.peek().step;
    }
}
