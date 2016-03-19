package reo.runtime;

import java.util.Hashtable;

public class CustomRObject extends AbstractRObject {
    private Hashtable<String, RObject> slots = new Hashtable<>();
    private RObject prototype; // Method on local cache-miss

    public CustomRObject(RObject prototype) {
        this.prototype = prototype;
    }

    @Override
    public RObject resolve(Evaluation evaluation, String selector) {
        RObject object = slots.get(selector);
        if(object != null)
            return object;

        return resolveOnCacheMiss(evaluation, selector);
    }

    private RObject resolveOnCacheMiss(Evaluation evaluation, String selector) {
        if(prototype != null) // Perhaps, a resolve-message should be sent to prototype?
            return prototype.resolve(evaluation, selector);
        throw new RuntimeException("Cannot resolve " + selector);
    }

    public void put(String selector, RObject value) {
        slots.put(selector, value);
    }
}
