package reo.runtime;

import java.util.List;

public interface RObject {
    RObject resolve(Evaluation evaluation, String selector);
    void send(Evaluation evaluation, String selector, RObject[] arguments);
    void apply(Evaluation evaluation, RObject receiver, RObject[] arguments);
}
