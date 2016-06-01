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

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    ConstructorCell constructorCell = new ConstructorCell(source, c -> ComponentParser.parse(workspace, c));
                    sendReplaceValue(constructorCell);
                }
            }
        });

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
