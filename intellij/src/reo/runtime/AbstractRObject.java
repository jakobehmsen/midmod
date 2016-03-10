package reo.runtime;

import java.util.HashSet;
import java.util.Hashtable;

public abstract class AbstractRObject implements RObject {
    private HashSet<String> prototypeSlots = new HashSet<>();
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

        return prototypeSlots.stream()
            .map(x -> slots.get(x).resolve(evaluation, selector))
            .filter(x -> x != null)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cannot resolve " + selector));
    }

    @Override
    public void apply(Evaluation evaluation, RObject receiver, RObject[] arguments) {
        send(evaluation, "apply", new RObject[]{receiver, new RArray(arguments)});
    }

    public void put(String selector, RObject value) {
        prototypeSlots.remove(selector);
        slots.put(selector, value);
    }

    public void putPrototype(String selector, RObject value) {
        prototypeSlots.add(selector);
        slots.put(selector, value);
    }
}
