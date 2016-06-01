package paidia;

import javax.swing.*;

public class ParameterCell extends AbstractValue {
    private Workspace workspace;

    public ParameterCell(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public ViewBinding toComponent() {
        JButton view = new JButton("?");

        view.addActionListener(e -> {
            ConstructorCell constructorCell = new ConstructorCell("", c -> ComponentParser.parse(workspace, c));
            sendReplaceValue(constructorCell);
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
        return "?";
    }
}
