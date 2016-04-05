package reo_OLD.runtime;

public class DoubleRObject extends PrimitiveRObject {
    private double value;

    public DoubleRObject(double value) {
        this.value = value;
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getDoublePrototype();
    }
}
