package reo.runtime;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public abstract class AbstractRObject implements RObject {
    private Hashtable<String, RObject> slots = new Hashtable<>();

    @Override
    public RObject send(Evaluation evaluation, String selector, List<RObject> arguments) {
        RObject method = slots.get(selector);
        if(method == null)
            method = resolve(evaluation, selector);

        return method.apply(evaluation, this, arguments);
    }

    @Override
    public void send2(Evaluation evaluation, String selector, RObject[] arguments) {
        RObject method = slots.get(selector);
        if(method == null)
            method = resolve(evaluation, selector);

        method.apply2(evaluation, this, arguments);
    }

    @Override
    public RObject resolve(Evaluation evaluation, String selector) {
        RObject object = slots.get(selector);
        if(object != null)
            return object;
        throw new RuntimeException("Cannot resolve " + selector);
    }

    @Override
    public RObject apply(Evaluation evaluation, RObject receiver, List<RObject> arguments) {
        return send(evaluation, "apply", Arrays.asList(receiver, new RArray(arguments.toArray(new RObject[arguments.size()]))));
    }

    @Override
    public void apply2(Evaluation evaluation, RObject receiver, RObject[] arguments) {

    }

    public void put(String selector, RObject value) {
        slots.put(selector, value);
    }
}
