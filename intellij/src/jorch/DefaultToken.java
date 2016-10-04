package jorch;

import java.util.Stack;

/**
 * Created by jakob on 04-10-16.
 */
public class DefaultToken implements Token {
    private static class Frame {
        private Step step;
        private Step callSite;

        private Frame(Step step, Step callSite) {
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

    private Stack<Frame> frames = new Stack<>();
    private Step nextStep;

    @Override
    public void moveForward(Step step) {
        frames.peek().setStep(step);
        nextStep = step;
    }

    @Override
    public void moveOut() {
        Frame frame = frames.pop();
        nextStep = frame.getCallSite();
    }

    @Override
    public void moveInto(Step step, Step callSite) {
        frames.push(new Frame(step, callSite));
        nextStep = step;
    }

    public Step getCurrentStep() {
        return frames.peek().getStep();
    }

    public void proceed() {
        nextStep.perform(this);
    }
}
