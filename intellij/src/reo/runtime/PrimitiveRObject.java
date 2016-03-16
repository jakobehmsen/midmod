package reo.runtime;

public abstract class PrimitiveRObject extends AbstractRObject {
    @Override
    protected RObject resolveOnCacheMiss(Evaluation evaluation, String selector) {
        return getPrototype(evaluation.getUniverse()).resolve(evaluation, selector);
    }

    protected abstract RObject getPrototype(Universe universe);
}
