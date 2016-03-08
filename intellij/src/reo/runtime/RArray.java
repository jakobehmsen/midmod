package reo.runtime;

public class RArray extends AbstractRObject {
    private RObject[] items;

    public RArray(RObject[] items) {
        this.items = items;
    }

    @Override
    public RObject resolve(Evaluation evaluation, String selector) {
        return evaluation.getUniverse().getArrayPrototype().resolve(evaluation, selector);
    }
}
