package midmod.pal.evaluation;

public class EvaluationContext {
    private boolean running = true;
    private Frame frame;

    public EvaluationContext(Frame frame) {
        this.frame = frame;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    public Frame getFrame() {
        return frame;
    }
}
