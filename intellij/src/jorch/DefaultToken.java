package jorch;

import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * Created by jakob on 04-10-16.
 */
public class DefaultToken implements Token {
    private static class FrameStep {
        private Step step;
        private Step callSite;

        private FrameStep(Step step, Step callSite) {
            this.step = step;
            this.callSite = callSite;
        }

        public Step getStep() {
            return step;
        }

        public void setStep(Step step) {
            this.step = step;
        }

        public Step getCallSite() {
            return callSite;
        }
    }

    private static class Frame {
        private Map<String, Object> locals;
        private Stack<FrameStep> frames = new Stack<>();
        private Consumer<Map<String, Object>> ouputMapper;

        private Frame(Map<String, Object> locals, Consumer<Map<String, Object>> ouputMapper) {
            this.locals = locals;
            this.ouputMapper = ouputMapper;
        }

        public Map<String, Object> getLocals() {
            return locals;
        }

        public Consumer<Map<String, Object>> getOuputMapper() {
            return ouputMapper;
        }
    }

    private Stack<Frame> frames = new Stack<>();
    private Step nextStep;

    @Override
    public void moveForward(Step step) {
        frames.peek().frames.peek().setStep(step);
        nextStep = step;
    }

    @Override
    public void moveOut() {
        FrameStep frame = frames.peek().frames.pop();
        nextStep = frame.getCallSite();
    }

    @Override
    public void moveInto(Step step, Step callSite) {
        frames.peek().frames.push(new FrameStep(step, callSite));
        nextStep = step;
    }

    @Override
    public void enterFrame(Consumer<Map<String, Object>> inputSupplier, Consumer<Map<String, Object>> ouputMapper) {
        Hashtable<String, Object> locals = new Hashtable<>();
        inputSupplier.accept(locals);

        frames.push(new Frame(locals, ouputMapper));
    }

    @Override
    public void exitFrame() {
        Map<String, Object> outerLocals = frames.size() > 1 ?  frames.get(frames.size() - 2).getLocals() : new Hashtable<>();
        frames.peek().getOuputMapper().accept(outerLocals);
        frames.pop();
    }

    @Override
    public Object getValue(String name) {
        return frames.peek().getLocals().get(name);
    }

    @Override
    public void setValue(String name, Object value) {
        frames.peek().getLocals().put(name, value);
    }

    public Step getCurrentStep() {
        return frames.peek().frames.peek().getStep();
    }

    public void proceed() {
        nextStep.perform(this);
    }
}
