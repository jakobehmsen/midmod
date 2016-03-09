package reo.runtime;

import java.util.List;

public class Evaluation {
    private Frame frame;

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    private Universe universe;
    private List<RObject> arguments;
    private RObject returnedValue;
    private RObject receiver;

    public Evaluation(Universe universe, RObject receiver, List<RObject> arguments) {
        this.universe = universe;
        this.receiver = receiver;
        this.arguments = arguments;
    }

    public boolean hasReturned() {
        return returnedValue != null;
    }

    public RObject valueReturned() {
        return returnedValue;
    }

    public Universe getUniverse() {
        return universe;
    }

    public RObject getArgument(int index) {
        return arguments.get(index);
    }

    public void returnValue(RObject value) {
        returnedValue = value;
    }

    public RObject getReceiver() {
        return receiver;
    }

    private boolean run;

    public void evaluate() {
        run = true;

        while(run)
            getFrame().evaluate(this);
    }

    public void halt() {
        run = false;
    }
}
