package reo_OLD.runtime;

public abstract class PrimitiveRObject extends AbstractRObject {
    @Override
    public RObject resolve(Evaluation evaluation, String selector) {
        return getPrototype(evaluation.getUniverse()).resolve(evaluation, selector);
    }

    protected abstract RObject getPrototype(Universe universe);
}
