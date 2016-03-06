package reo.runtime;

import java.util.List;

public class FunctionRObject extends AbstractRObject {
    private Statement block;

    public FunctionRObject(Statement block) {
        this.block = block;
    }

    @Override
    public RObject apply(Evaluation evaluation, List<RObject> arguments) {
        Evaluation applyEvaluation = new Evaluation(evaluation.getUniverse(), arguments);
        block.perform(applyEvaluation);
        return applyEvaluation.valueReturned();
    }
}
