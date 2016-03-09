package reo.runtime;

import java.util.List;

public interface RObject {
    RObject resolve(Evaluation evaluation, String selector);
    RObject send(Evaluation evaluation, String selector, List<RObject> arguments);
    void send2(Evaluation evaluation, String selector, RObject[] arguments);
    RObject apply(Evaluation evaluation, RObject receiver, List<RObject> arguments);
    void apply2(Evaluation evaluation, RObject receiver, RObject[] arguments);
}
