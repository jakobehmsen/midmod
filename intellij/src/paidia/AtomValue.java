package paidia;

import javax.swing.*;

public class AtomValue extends AbstractValue {
    private Workspace workspace;
    private String source;
    private String text;
    private Object number;

    public AtomValue(Workspace workspace, String source, String text, Object number) {
        this.workspace = workspace;
        this.source = source;
        this.text = text;
        this.number = number;
    }

    public Object getValue() {
        return number;
    }

    @Override
    public ViewBinding toComponent() {
        JLabel view = new JLabel(text);

        view.setSize(view.getPreferredSize());

        //workspace.setupView(this, view, () -> source, newValue -> sendReplaceValue(newValue));

        return new ViewBinding() {
            AtomValue theValue = AtomValue.this;

            {
                //workspace.setupView(() -> theValue, this, () -> theValue.toSource(), newValue -> theValue.sendReplaceValue(newValue));
            }

            @Override
            public JComponent getView() {
                return view;
            }

            @Override
            public void release() {

            }

            @Override
            public boolean isCompatibleWith(Value value) {
                return value instanceof AtomValue;
            }

            @Override
            public void updateFrom(Value value) {
                view.setText(((AtomValue)value).text);
                theValue = (AtomValue)value;
                view.setSize(view.getPreferredSize());
            }

            @Override
            public void setupWorkspace(Workspace workspace) {
                workspace.setupView(() -> theValue, this, () -> theValue.toSource(), newValue -> theValue.sendReplaceValue(newValue));
            }
        };
    }

    @Override
    public String toSource() {
        return source;
    }

    @Override
    public Value reduce() {
        return this;
    }
}
