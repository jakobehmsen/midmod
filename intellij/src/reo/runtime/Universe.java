package reo.runtime;

import java.util.Arrays;

public class Universe {
    private CustomRObject integerPrototype;
    private CustomRObject arrayPrototype;
    private CustomRObject functionPrototype;

    public Universe() {
        integerPrototype = new CustomRObject();
        integerPrototype.put("+", new FunctionRObject(new Behavior(new Instruction[]{
            Instructions.loadLocal(0),
            Instructions.loadLocal(1),
            Instructions.addi(),
            Instructions.ret()
        })));
        arrayPrototype = new CustomRObject();
        functionPrototype = new CustomRObject();
    }

    public RObject getIntegerPrototype() {
        return integerPrototype;
    }

    public RObject evaluate(Behavior behavior, RObject receiver) {
        Evaluation evaluation = new Evaluation(this, receiver, Arrays.asList());
        evaluation.setFrame(behavior.createFrame(null));
        evaluation.getFrame().push(receiver);
        evaluation.evaluate();
        return evaluation.getFrame().peek();
        //statement.perform(evaluation);
        //return evaluation.valueReturned();
    }

    public RObject getArrayPrototype() {
        return arrayPrototype;
    }

    public RObject getFunctionPrototype() {
        return functionPrototype;
    }
}
