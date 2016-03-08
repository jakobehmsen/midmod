package reo.runtime;

public class RArray extends PrimitiveRObject {
    private RObject[] items;

    public RArray(RObject[] items) {
        this.items = items;
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getArrayPrototype();
    }
}
