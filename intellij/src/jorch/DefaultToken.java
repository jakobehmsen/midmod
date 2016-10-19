package jorch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

public class DefaultToken implements Token, Serializable {
    private Stack<Frame> frames = new Stack<>();

    public DefaultToken(Map<String, Object> context, Step step) {
        perform(context, step, new Finish());
    }

    @Override
    public void perform(Map<String, Object> context, Step step, Step callback) {
        Frame frame = new Frame(context, step, callback);
        frames.push(frame);

        schedule(token ->
            token.frames.peek().step.perform(token, frame.context));
    }

    @Override
    public void moveNext() {
        schedule(token -> {
            Frame frame = token.frames.pop();
            frame.callback.perform(token, frame.context);
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

        private void writeObject(ObjectOutputStream oos)
            throws IOException {
            oos.writeObject(context);
            oos.writeObject(step);
            oos.writeObject(callback);
        }

        private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
            context = (Map<String, Object>) ois.readObject();
            step = (Step) ois.readObject();
            callback = (Step) ois.readObject();
        }
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

    @Override
    public void halt() { }

    protected void finished() { }

    protected void schedule(DefaultTokenConsumer runnable) { }

    @Override
    public void proceed() { }

    protected interface DefaultTokenConsumer extends Consumer<DefaultToken>, Serializable {

    }

    private void writeObject(ObjectOutputStream oos)
        throws IOException {
        oos.writeObject(frames);
        oos.toString();
    }

    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        frames = (Stack<Frame>) ois.readObject();
    }
}
