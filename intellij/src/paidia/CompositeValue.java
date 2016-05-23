package paidia;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.util.List;
import java.util.function.Function;

public class CompositeValue extends JPanel implements Value {
    private List<String> parameters;
    private String operationName;
    private Function<Object[], Object> reducer;

    public CompositeValue(List<String> parameters, List<Value> values, String operationName, Function<Object[], Object> reducer) {
        this.parameters = parameters;
        this.operationName = operationName;
        this.reducer = reducer;

        int operationNameIndex = 1;

        int width = 0;

        for(int i = 0; i < parameters.size(); i++) {
            if(i == operationNameIndex) {
                JLabel operationView = new JLabel(operationName);
                /*JComponent operationView = (JComponent)values.get(i);
                values.get(i).bindTo(new Parameter() {
                    @Override
                    public void removeValue() {

                    }

                    @Override
                    public void replaceValue(Value value) {
                        value.toString();
                    }
                });*/
                operationView.setSize(((ComponentUI) operationView.getUI()).getPreferredSize(operationView));
                add(operationView);

                width += operationView.getWidth();
            }

            JComponent parameterView = (JComponent)values.get(i);
            //parameterView.setSize(((ComponentUI) parameterView.getUI()).getPreferredSize(parameterView));
            values.get(i).bindTo(new Parameter() {
                @Override
                public void removeValue() {

                }

                @Override
                public void replaceValue(Value value) {
                    value.toString();
                }
            });
            add(parameterView);

            width += parameterView.getWidth();
        }

        //setSize(width + 25, 30);
        setBorder(BorderFactory.createRaisedSoftBevelBorder());

        setPreferredSize(getPreferredSize());
        setSize(getPreferredSize());
    }

    @Override
    public void bindTo(Parameter parameter) {

    }

    @Override
    public void unbind() {

    }
}
