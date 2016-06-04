package paidia;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CompositeValue extends AbstractValue {
    private List<String> parameters;
    private String operationName;
    private Function<Value[], Value> reducer;
    private List<Value> values;
    private Workspace workspace;
    private String source;
    private Function<String, String> sourceWrapper;

    public CompositeValue(List<String> parameters, List<Value> values, String operationName, Function<Value[], Value> reducer, Workspace workspace, String source, Function<String, String> sourceWrapper) {
        this.parameters = parameters;
        this.operationName = operationName;
        this.reducer = reducer;
        this.values = values;
        this.workspace = workspace;
        this.source = source;
        this.sourceWrapper = sourceWrapper;
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
                }
            };
            parameterView.getView().addComponentListener(componentAdapter);

            parameterView.setupWorkspace(workspace);

            values.get(i).addUsage(new Usage() {
                ViewBinding valueView = parameterView;

                @Override
                public void removeValue() {

                }

                @Override
                public void replaceValue(Value value) {
                    if(!values.get(theI).equals(value)) {
                        if (valueView != null) {
                            valueView.getView().removeComponentListener(componentAdapter);
                            valueView.release();
                        }

                        ViewBinding valueAsComponent = value.toComponent();

                        valueAsComponent.setupWorkspace(workspace);

                        valueAsComponent.getView().addComponentListener(componentAdapter);

                        view.remove(theZIndex);
                        view.add(valueAsComponent.getView(), theZIndex);
                        view.setPreferredSize(view.getLayout().preferredLayoutSize(view));
                        view.setSize(view.getPreferredSize());

                        values.set(theI, value);
                        value.addUsage(this);

                        valueView = valueAsComponent;
                    }

                    sendReplaceValue(CompositeValue.this);
                }
            });

            view.add(parameterView.getView());
            zIndex++;
        }

        view.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        view.setSize(view.getPreferredSize());

        return new ViewBinding() {
            {
                //workspace.setupView(() -> CompositeValue.this, this, () -> toSource(), newValue -> sendReplaceValue(newValue));
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
                return false;
            }

            @Override
            public void updateFrom(Value value) {

            }

            @Override
            public void setupWorkspace(Workspace workspace) {
                workspace.setupView(() -> CompositeValue.this, this, () -> toSource(), newValue -> sendReplaceValue(newValue));
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

        return sourceWrapper.apply(allSource);
    }

    @Override
    public Value reduce() {
        Value[] reductions = values.stream().map(x -> x.reduce()).toArray(s -> new Value[s]);
        return reducer.apply(reductions);

        /*return new AbstractValue() {
            Value[] reductions = values.stream().map(x -> x.createProjection()).toArray(s -> new Value[s]);
            Value theValue = reducer.apply(reductions);

            {
                CompositeValue.this.addUsage(new Usage() {
                    @Override
                    public void removeValue() {

                    }

                    @Override
                    public void replaceValue(Value value) {
                        if(value == CompositeValue.this) {
                            reductions = values.stream().map(x -> x.createProjection()).toArray(s -> new Value[s]);
                            try {
                                theValue = reducer.apply(reductions);
                                sendReplaceValue(theValue);
                            } catch(Exception e) {
                                // Don't indicate value replacement; perhaps an error should be indicated?
                                //sendReplaceValue(new AtomValue(workspace, "\"" + e.getMessage() + "\"", e.getMessage()));
                            }
                        } else
                            sendReplaceValue(value.createProjection());
                    }
                });
            }

            @Override
            public ViewBinding toComponent() {
                return theValue.toComponent();
            }

            @Override
            public String toSource() {
                return theValue.toSource();
            }

            @Override
            public Value createProjection() {
                return this;
            }
        };*/
    }
}
