package reo_OLD.runtime;

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

    @Override
    public String toString() {
        return "" + value;
    }

    @Override
    public Object toNative() {
        return (int)value;
    }
}
