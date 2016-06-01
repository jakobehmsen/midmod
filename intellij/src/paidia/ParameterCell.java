package paidia;

import javax.swing.*;

public class ParameterCell extends AbstractValue {
    //private Parameter parameter;
    private Workspace workspace;

    public ParameterCell(Workspace workspace) {
        this.workspace = workspace;
        /*super("?");

        addActionListener(e -> {
            //workspace.construct(this, parameter);

            ConstructorCell constructorCell = new ConstructorCell(c -> ComponentParser.parse(workspace, c));
            parameter.replaceValue(constructorCell);
            //constructorCell.requestFocusInWindow();
        });*/
    }

    @Override
    public ViewBinding toComponent() {
        JButton view = new JButton("?");

        view.addActionListener(e -> {
            //workspace.construct(this, parameter);

            ConstructorCell constructorCell = new ConstructorCell("", c -> ComponentParser.parse(workspace, c));
            //parameter.replaceValue(constructorCell);
            sendReplaceValue(constructorCell);
            //constructorCell.requestFocusInWindow();
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
