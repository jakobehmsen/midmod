package reo.runtime;

public class Evaluation {
    private Frame frame;
    private boolean running;

    public Evaluation(Frame frame) {
        this.frame = frame;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public void evaluate() {
        running = true;
        while(running)
            frame.evaluate(this);
    }
}
