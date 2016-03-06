package reo.runtime;

import java.util.Arrays;

public class Universe {
    private CustomRObject integerPrototype;

    public Universe() {
        integerPrototype = new CustomRObject();
        integerPrototype.put("+", new FunctionRObject(evaluation ->
            evaluation.returnValue(
                new IntegerRObject(((IntegerRObject) evaluation.getArgument(0)).getValue() + ((IntegerRObject) evaluation.getArgument(1)).getValue()))
        ));
    }

    public RObject getIntegerPrototype() {
        return integerPrototype;
    }

    public RObject evaluate(Statement statement) {
        Evaluation evaluation = new Evaluation(this, Arrays.asList());
        statement.perform(evaluation);
        return evaluation.valueReturned();
    }
}
