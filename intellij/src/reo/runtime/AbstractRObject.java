package reo.runtime;

import java.util.Hashtable;
import java.util.List;

public abstract class AbstractRObject implements RObject {
    private Hashtable<String, RObject> slots = new Hashtable<>();

    @Override
    public RObject send(Evaluation evaluation, String selector, List<RObject> arguments) {
        RObject method = slots.get(selector);
        if(method == null)
            method = resolve(evaluation, selector);

        return method.apply(evaluation, arguments);
    }

    @Override
    public RObject resolve(Evaluation evaluation, String selector) {
        throw new RuntimeException("Cannot resolve " + selector);
    }

    @Override
    public RObject apply(Evaluation evaluation, List<RObject> arguments) {
        return send(evaluation, "apply", arguments);
    }

    public void put(String selector, RObject value) {
        slots.put(selector, value);
    }
}
