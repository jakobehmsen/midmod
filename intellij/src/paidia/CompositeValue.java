package paidia;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

        int zIndex = 0;
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
                zIndex++;

                width += operationView.getWidth();
            }

            int theZIndex = zIndex;
            JComponent parameterView = (JComponent)values.get(i);
            int theI = i;

            Parameter param = new Parameter() {
                @Override
                public void removeValue() {

                }

                @Override
                public void replaceValue(Value value) {
                    values.get(theI).unbind();

                    remove(theZIndex);
                    add(((JComponent)value), theZIndex);
                    setPreferredSize(getLayout().preferredLayoutSize(CompositeValue.this));
                    setSize(getPreferredSize());

                    values.set(theI, value);
                    value.bindTo(this);

                    revalidate();
                    repaint();
                }
            };

            //parameterView.setSize(((ComponentUI) parameterView.getUI()).getPreferredSize(parameterView));

            ComponentAdapter componentAdapter = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    setPreferredSize(getLayout().preferredLayoutSize(CompositeValue.this));
                    setSize(getPreferredSize());
                }
            };
            ((JComponent)values.get(theI)).addComponentListener(componentAdapter);

            values.get(i).bindTo(new Parameter() {
                @Override
                public void removeValue() {

                }

                @Override
                public void replaceValue(Value value) {
                    ((JComponent)values.get(theI)).removeComponentListener(componentAdapter);

                    values.get(theI).unbind();

                    ((JComponent)value).addComponentListener(componentAdapter);

                    remove(theZIndex);
                    add(((JComponent)value), theZIndex);
                    setPreferredSize(getLayout().preferredLayoutSize(CompositeValue.this));
                    setSize(getPreferredSize());

                    values.set(theI, value);
                    value.bindTo(this);

                    revalidate();
                    repaint();
                }
            });
            add(parameterView);
            zIndex++;

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
