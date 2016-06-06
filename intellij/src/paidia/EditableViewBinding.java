package paidia;

import javax.swing.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EditableViewBinding implements ViewBinding {
    private Value value;
    private Workspace workspace;
    private ViewBinding viewBinding;

    public EditableViewBinding(Value initialValue, ViewBinding initialViewBinding, Workspace workspace, BiConsumer<Value, ViewBinding> valueReplacer, Consumer<JComponent> viewReplacer) {
        this.viewBinding = initialViewBinding;
        value = initialValue;
        workspace.setupView(() -> value, this, () -> value.toSource(), newValue -> {
            value = newValue;
            viewBinding = value.toComponent();
            valueReplacer.accept(value, viewBinding);
        }, newView -> {
            viewReplacer.accept(newView);
        });
    }

    @Override
    public JComponent getView() {
        return viewBinding.getView();
    }

    @Override
    public void release() {
        viewBinding.release();
    }

    @Override
    public boolean isCompatibleWith(Value value) {
        return viewBinding.isCompatibleWith(value);
    }

    @Override
    public void updateFrom(Value value) {
        viewBinding.updateFrom(value);
    }
}
