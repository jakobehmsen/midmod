package jorch;

import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;

public class DefaultToken implements Token {
    private Stack<Frame> frames = new Stack<>();
    private Runnable nextPart;

    public DefaultToken(Map<String, Object> context, Step step2, Step callback) {
        perform(context, step2, callback);
    }

    @Override
    public void perform(Map<String, Object> context, Step step, Step callback) {
        Frame frame = new Frame(context, step, callback);
        frames.push(frame);
        nextPart = () -> {
            performTask(() -> {
                frame.step.perform(this, frame.context);
            });
        };
    }

    protected void performTask(Runnable taskPerformer) {
        taskPerformer.run();
    }

    @Override
    public void moveNext() {
        Frame frame = frames.pop();
        nextPart = () -> frame.callback.perform(this, frame.context);
    }

    protected void moveNextTask(Runnable moveNextPerformer) {
        moveNextPerformer.run();
    }

    private static class Frame {
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
        Runnable part = nextPart;
        nextPart = null;
        part.run();
    }

    public static void run(Map<String, Object> context, Step step) {
        boolean[] finished = new boolean[1];

        DefaultToken token = new DefaultToken(context, step, (t, ctx) -> {
            finished[0] = true;
        });

        while(!finished[0]) {
            token.proceed();
        }
    }
}
