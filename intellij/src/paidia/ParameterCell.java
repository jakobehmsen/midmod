package paidia;

import javax.swing.*;

public class ParameterCell extends JButton implements Value {
    private Parameter parameter;

    public ParameterCell(Workspace workspace) {
        super("?");

        addActionListener(e -> {
            //workspace.construct(this, parameter);

            parameter.replaceValue(new ConstructorCell(c -> ComponentParser.parse(workspace, c)));
        });
    }

    @Override
    public void bindTo(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void unbind() {
        parameter = null;
    }
}
