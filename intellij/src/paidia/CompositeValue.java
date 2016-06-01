package paidia;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class CompositeValue extends AbstractValue {
    private List<String> parameters;
    private String operationName;
    private Function<Object[], Object> reducer;
    private List<Value> values;
    private Workspace workspace;
    private String source;

    public CompositeValue(List<String> parameters, List<Value> values, String operationName, Function<Object[], Object> reducer, Workspace workspace, String source) {
        this.parameters = parameters;
        this.operationName = operationName;
        this.reducer = reducer;
        this.values = values;
        this.workspace = workspace;
        this.source = source;

        /*int operationNameIndex = 1;

        int zIndex = 0;
        for(int i = 0; i < parameters.size(); i++) {
            if(i == operationNameIndex) {
                JLabel operationView = new JLabel(operationName);
                operationView.setSize(((ComponentUI) operationView.getUI()).getPreferredSize(operationView));
                add(operationView);
                zIndex++;
            }

            int theZIndex = zIndex;
            JComponent parameterView = (JComponent)values.get(i);
            int theI = i;

            ComponentAdapter componentAdapter = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    setPreferredSize(getLayout().preferredLayoutSize(CompositeValue.this));
                    setSize(getPreferredSize());

                    revalidate();
                    repaint();
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
        }

        setBorder(BorderFactory.createRaisedSoftBevelBorder());

        setPreferredSize(getPreferredSize());
        setSize(getPreferredSize());*/
    }

    private int operationNameIndex = 1;

    @Override
    public ViewBinding toComponent() {
        JPanel view = new JPanel();

        int zIndex = 0;
        for(int i = 0; i < parameters.size(); i++) {
            if(i == operationNameIndex) {
                JLabel operationView = new JLabel(operationName);
                operationView.setSize(((ComponentUI) operationView.getUI()).getPreferredSize(operationView));
                view.add(operationView);
                zIndex++;
            }

            int theZIndex = zIndex;
            Value parameter = values.get(i);
            ViewBinding parameterView = parameter.toComponent();
            int theI = i;

            ComponentAdapter componentAdapter = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    view.setPreferredSize(view.getLayout().preferredLayoutSize(view));
                    view.setSize(view.getPreferredSize());

                    //view.revalidate();
                    //view.repaint();
                }
            };
            parameterView.getView().addComponentListener(componentAdapter);

            values.get(i).addUsage(new Usage() {
                ViewBinding valueView = parameterView;

                @Override
                public void removeValue() {

                }

                @Override
                public void replaceValue(Value value) {
                    if(valueView != null) {
                        valueView.getView().removeComponentListener(componentAdapter);
                        valueView.release();
                    }

                    ViewBinding valueAsComponent = value.toComponent();

                    valueAsComponent.getView().addComponentListener(componentAdapter);

                    view.remove(theZIndex);
                    view.add(valueAsComponent.getView(), theZIndex);
                    view.setPreferredSize(view.getLayout().preferredLayoutSize(view));
                    view.setSize(view.getPreferredSize());

                    values.set(theI, value);
                    value.addUsage(this);

                    valueView = valueAsComponent;
                }
            });

            /*values.get(i).bindTo(new Parameter() {
                ViewBinding valueView = parameterView;

                @Override
                public void removeValue() {

                }

                @Override
                public void replaceValue(Value value) {
                    if(valueView != null) {
                        valueView.getView().removeComponentListener(componentAdapter);
                        valueView.release();
                    }

                    values.get(theI).unbind();

                    ViewBinding valueAsComponent = value.toComponent();

                    valueAsComponent.getView().addComponentListener(componentAdapter);

                    view.remove(theZIndex);
                    view.add(valueAsComponent.getView(), theZIndex);
                    view.setPreferredSize(view.getLayout().preferredLayoutSize(view));
                    view.setSize(view.getPreferredSize());

                    values.set(theI, value);
                    value.bindTo(this);

                    valueView = valueAsComponent;
                }
            });*/
            view.add(parameterView.getView());
            zIndex++;
        }

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    String initialSource = toSource();

                    ConstructorCell constructorCell = new ConstructorCell(initialSource, c -> ComponentParser.parse(workspace, c));
                    //parameter.replaceValue(constructorCell);
                    sendReplaceValue(constructorCell);
                }
            }
        });

        view.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        view.setSize(view.getPreferredSize());

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
        String allSource = "";

        for(int i = 0; i < values.size(); i++) {
            if(i == operationNameIndex)
                allSource += source;
            allSource += values.get(i).toSource();
        }

        // Combine source with parameters
        // - what if not all parameters are set?
        // - have special syntax for this?
        //   - e.g.: ? + ?

        return allSource;
    }
}
