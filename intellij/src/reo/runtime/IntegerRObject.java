package reo.runtime;

public class IntegerRObject extends PrimitiveRObject {
    private long value;

    public IntegerRObject(long value) {
        this.value = value;
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getIntegerPrototype();
    }

    public long getValue() {
        return value;
    }
}
