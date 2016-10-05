package jorch;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by jakob on 04-10-16.
 */
public interface Token {
    void moveForward(Step step);
    void moveOut();
    void moveInto(Step step, Step callSite);
    void enterFrame(Consumer<Map<String, Object>> inputSupplier, Consumer<Map<String, Object>> ouputMapper);
    void exitFrame();
    Object getValue(String name);
    void setValue(String name, Object value);
}
