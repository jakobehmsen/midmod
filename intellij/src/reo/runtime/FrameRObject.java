package reo.runtime;

public class FrameRObject extends PrimitiveRObject {
    private Frame frame;

    public FrameRObject(Frame frame) {
        this.frame = frame;
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getFramePrototype();
    }
}
