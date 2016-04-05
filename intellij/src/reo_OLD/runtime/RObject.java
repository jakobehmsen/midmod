package reo_OLD.runtime;

public interface RObject {
    RObject resolve(Evaluation evaluation, String selector);
    void send(Evaluation evaluation, String selector, RObject[] arguments);
    void apply(Evaluation evaluation, RObject receiver, RObject[] arguments);
    default Object toNative() { return this; }
}
