package paidia;

import javax.swing.*;
import java.util.function.Function;

public class EmbeddedValue extends AbstractValue {
    private String prefix;
    private String suffix;
    private Value value;

    public EmbeddedValue(String prefix, String suffix, Value value) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.value = value;

        value.addUsage(new Usage() {
            @Override
            public void removeValue() {
                sendRemoveValue();
            }

            @Override
            public void replaceValue(Value value) {
                if(EmbeddedValue.this.value == value) {
                    sendReplaceValue(EmbeddedValue.this);
                    return;
                }

                sendReplaceValue(value);
            }
        });
    }

    @Override
    public ViewBinding toComponent() {
        ViewBinding viewBinding = value.toComponent();

        return new ViewBinding() {
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

            @Override
            public void setupEditor(ConstructorCell editor) {
                Function<String, Value> parser = editor.getParser();
                editor.setParser(s ->
                    new EmbeddedValue(prefix, suffix, parser.apply(s)));
            }

            @Override
            public void setupWorkspace(Workspace workspace) {
                workspace.setupView(() -> EmbeddedValue.this, this, () -> value.toSource(), newValue -> sendReplaceValue(newValue));
            }
        };
    }

    @Override
    public String toSource() {
        return prefix + value.toSource() + suffix;
    }

    @Override
    public Value reduce() {
        return value.reduce();
    }
}
