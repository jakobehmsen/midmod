package reo.runtime;

public class NativeRObject extends PrimitiveRObject {
    private Object nativeObject;

    public NativeRObject(Object nativeObject) {
        this.nativeObject = nativeObject;
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getNativePrototype();
    }
}
