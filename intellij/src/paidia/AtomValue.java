package paidia;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AtomValue extends AbstractValue {
    private Workspace workspace;
    private String source;
    private Object number;

    public AtomValue(Workspace workspace, String source, Object number) {
        this.workspace = workspace;
        this.source = source;
        this.number = number;
    }

    @Override
    public ViewBinding toComponent() {
        JLabel view = new JLabel(source);

        view.setSize(view.getPreferredSize());

        workspace.setupView(view, () -> source, newValue -> sendReplaceValue(newValue));

        return new ViewBinding() {
            @Override
            public JComponent getView() {
                return view;
            }

            @Override
            public void release() {

            }
        };
    }

    @Override
    public String toSource() {
        return source;
    }
}
