package reo_OLD.runtime;

public class Evaluation {
    private Universe universe;
    private Frame frame;

    public Evaluation(Universe universe) {
        this.universe = universe;
    }

    public Universe getUniverse() {
        return universe;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    private boolean run;

    public void evaluate() {
        run = true;

        while(run) {
            try {
                while (run)
                    getFrame().evaluate(this);
            } catch (Exception exception) {
                getUniverse().getFramePrototype().send(this, "onNativeException/2", new RObject[]{new FrameRObject(getFrame()), new NativeRObject(exception)});
            }
        }
    }

    public void halt() {
        run = false;
    }

    private RObject[] operands = new RObject[0];

    public void popOperands(int count) {
        if(operands.length < count)
            operands = new RObject[count];
        getFrame().pop(operands, count);
    }

    public RObject getOperand(int index) {
        return operands[index];
    }
}
