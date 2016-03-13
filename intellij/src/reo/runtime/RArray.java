package reo.runtime;

import java.util.Arrays;

public class RArray extends PrimitiveRObject {
    private RObject[] items;

    public RArray(RObject[] items) {
        this.items = items;
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getArrayPrototype();
    }

    @Override
    public String toString() {
        return "#" + Arrays.toString(items);
    }
}
