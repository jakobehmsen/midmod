package paidia;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Workspace {
    void setupView(Supplier<Value> value, ViewBinding viewBinding, Supplier<String> sourceGetter, Consumer<Value> valueReplacer);
    void setupView(Supplier<Value> value, ViewBinding viewBinding, Supplier<String> sourceGetter, Consumer<Value> valueReplacer, Consumer<JComponent> viewReplacer);
}
