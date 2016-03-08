package reo.runtime;

import java.util.List;

public class FunctionRObject extends PrimitiveRObject {
    private Statement block;

    public FunctionRObject(Statement block) {
        this.block = block;
    }

    @Override
    public RObject apply(Evaluation evaluation, RObject receiver, List<RObject> arguments) {
        Evaluation applyEvaluation = new Evaluation(evaluation.getUniverse(), receiver, arguments);
        block.perform(applyEvaluation);
        return applyEvaluation.valueReturned();
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getFunctionPrototype();
    }
}
