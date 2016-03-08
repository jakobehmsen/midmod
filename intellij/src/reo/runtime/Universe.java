package reo.runtime;

import java.util.Arrays;

public class Universe {
    private CustomRObject integerPrototype;
    private CustomRObject arrayPrototype;

    public Universe() {
        integerPrototype = new CustomRObject();
        integerPrototype.put("+", new FunctionRObject(evaluation ->
            evaluation.returnValue(
                new IntegerRObject(((IntegerRObject) evaluation.getReceiver()).getValue() + ((IntegerRObject) evaluation.getArgument(0)).getValue()))
        ));
    }

    public RObject getIntegerPrototype() {
        return integerPrototype;
    }

    public RObject evaluate(Statement statement) {
        Evaluation evaluation = new Evaluation(this, null /*What should receiver be?*/, Arrays.asList());
        statement.perform(evaluation);
        return evaluation.valueReturned();
    }

    public RObject getArrayPrototype() {
        return arrayPrototype;
    }
}
