package reo.runtime;

import java.util.Hashtable;

public abstract class AbstractRObject implements RObject {
    private Hashtable<String, RObject> slots = new Hashtable<>();

    @Override
    public void send(Evaluation evaluation, String selector, RObject[] arguments) {
        RObject method = slots.get(selector);
        if(method == null)
            method = resolve(evaluation, selector);

        method.apply(evaluation, this, arguments);
    }

    @Override
    public RObject resolve(Evaluation evaluation, String selector) {
        RObject object = slots.get(selector);
        if(object != null)
            return object;
        throw new RuntimeException("Cannot resolve " + selector);
    }

    @Override
    public void apply(Evaluation evaluation, RObject receiver, RObject[] arguments) {
        send(evaluation, "apply", new RObject[]{receiver, new RArray(arguments)});
    }

    public void put(String selector, RObject value) {
        slots.put(selector, value);
    }
}
