package lazymade;

import jdk.nashorn.api.scripting.JSObject;

public interface Transformer {
    void transform(JSObject model);
}
