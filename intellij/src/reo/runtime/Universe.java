package reo.runtime;

import java.util.Arrays;

public class Universe {
    private CustomRObject integerPrototype;
    private CustomRObject arrayPrototype;
    private CustomRObject functionPrototype;

    public Universe() {
        integerPrototype = new CustomRObject();
        integerPrototype.put("+", new FunctionRObject(evaluation ->
            evaluation.returnValue(
                new IntegerRObject(((IntegerRObject) evaluation.getReceiver()).getValue() + ((IntegerRObject) evaluation.getArgument(0)).getValue()))
        ));
        arrayPrototype = new CustomRObject();
        functionPrototype = new CustomRObject();
    }

    public RObject getIntegerPrototype() {
        return integerPrototype;
    }

    public RObject evaluate(Statement statement, RObject receiver) {
        Evaluation evaluation = new Evaluation(this, receiver, Arrays.asList());
        statement.perform(evaluation);
        return evaluation.valueReturned();
    }

    public RObject getArrayPrototype() {
        return arrayPrototype;
    }

    public RObject getFunctionPrototype() {
        return functionPrototype;
    }
}
