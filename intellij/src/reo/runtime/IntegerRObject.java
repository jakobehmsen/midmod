package reo.runtime;

public class IntegerRObject extends AbstractRObject {
    private long value;

    public IntegerRObject(long value) {
        this.value = value;
    }

    @Override
    public RObject resolve(Evaluation evaluation, String selector) {
        return evaluation.getUniverse().getIntegerPrototype().resolve(evaluation, selector);
    }

    public long getValue() {
        return value;
    }
}
