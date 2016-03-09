package reo.runtime;

import java.util.List;

public class FunctionRObject extends PrimitiveRObject {
    /*private Statement block;

    public FunctionRObject(Statement block) {
        this.block = block;
    }*/

    private Behavior behavior;

    public FunctionRObject(Behavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public RObject apply(Evaluation evaluation, RObject receiver, List<RObject> arguments) {
        Evaluation applyEvaluation = new Evaluation(evaluation.getUniverse(), receiver, arguments);
        //block.perform(applyEvaluation);
        return applyEvaluation.valueReturned();
    }

    @Override
    public void apply2(Evaluation evaluation, RObject receiver, RObject[] arguments) {
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
