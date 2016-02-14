package midmod.pal.evaluation;

import midmod.rules.Environment;

import java.util.List;

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

    public void setFrame(Frame frame) {
        this.frame = frame;
    }
}
