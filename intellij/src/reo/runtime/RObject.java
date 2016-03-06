package reo.runtime;

import java.util.List;

public interface RObject {
    RObject resolve(Evaluation evaluation, String selector);
    RObject send(Evaluation evaluation, String selector, List<RObject> arguments);
    RObject apply(Evaluation evaluation, List<RObject> arguments);
}
