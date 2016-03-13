package reo.runtime;

public class FunctionRObject extends PrimitiveRObject {
    private Behavior behavior;

    public FunctionRObject(Behavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public void apply(Evaluation evaluation, RObject receiver, RObject[] arguments) {
        Frame frame = behavior.createFrame(evaluation.getFrame());
        frame.push(receiver);
        frame.push(arguments);
        evaluation.setFrame(frame);
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getFunctionPrototype();
    }
}
