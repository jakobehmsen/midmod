package reo.runtime;

public class RString extends PrimitiveRObject {
    private String value;

    public RString(String value) {
        this.value = value;
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getStringPrototype();
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Object toNative() {
        return value;
    }
}
