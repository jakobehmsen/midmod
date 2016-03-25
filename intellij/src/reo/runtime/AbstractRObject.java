package reo.runtime;

public abstract class AbstractRObject implements RObject {
    @Override
    public void send(Evaluation evaluation, String selector, RObject[] arguments) {
        // Probably, arguments should be used for method resolution also
        RObject method = resolve(evaluation, selector);

        method.apply(evaluation, this, arguments);
    }

    @Override
    public void apply(Evaluation evaluation, RObject receiver, RObject[] arguments) {
        send(evaluation, "apply/2", new RObject[]{receiver, new RArray(arguments)});
    }
}
