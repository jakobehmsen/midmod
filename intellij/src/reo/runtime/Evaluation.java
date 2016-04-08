package reo.runtime;

public class Evaluation {
    private Universe universe;
    private Frame frame;
    private boolean running;
    private Observable nullPrototype;

    public Evaluation(Universe universe, Frame frame) {
        this.universe = universe;
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

    public void halt() {
        running = false;
    }

    private Observable[] operands = new Observable[0];

    public void popOperands(int count) {
        if(operands.length < count)
            operands = new Observable[count];

        for(int i = 0; i < count; i++)
            operands[i] = getFrame().pop();
    }

    public Observable getOperand(int ordinal) {
        return operands[ordinal];
    }

    public Observable[] getOperands() {
        return operands;
    }

    public Universe getUniverse() {
        return universe;
    }
}
