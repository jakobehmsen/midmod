package reo.runtime;

public class CustomRObject extends AbstractRObject {
    private RObject prototype; // Method on local cache-miss

    public CustomRObject(RObject prototype) {
        this.prototype = prototype;
    }

    @Override
    protected RObject resolveOnCacheMiss(Evaluation evaluation, String selector) {
        if(prototype != null) // Perhaps, a resolve-message should be sent to prototype?
            return prototype.resolve(evaluation, selector);
        throw new RuntimeException("Cannot resolve " + selector);
    }
}
