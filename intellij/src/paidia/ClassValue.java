package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.List;

public class ClassValue extends AbstractValue2 implements Value2Observer {
    public List<ParameterValue> getClassParameters() {
        return parameters;
    }

    public Value2 getSelectorAsApplication() {
        return new AbstractValue2() {
            @Override
            public ViewBinding2 toView(PlaygroundView playgroundView) {
                JPanel view = new JPanel(null);

                view.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                view.setPreferredSize(new Dimension(200, 75));

                selectorValues.forEach(x -> {
                    JComponent selectorPartView = x.value.forApplication().toView(playgroundView).getComponent();
                    selectorPartView.setLocation(x.location);
                    view.add(selectorPartView);
                });

                return new ViewBinding2() {
                    @Override
                    public JComponent getComponent() {
                        return view;
                    }
                };
            }

            @Override
            public String getText() {
                return null;
            }

            @Override
            public String getSource(TextContext textContext) {
                return null;
            }

            @Override
            public Value2 reduce(Map<String, Value2> environment) {
                return null;
            }
        };
    }

    private static class PositionedValue {
        private Point location;
        private Value2 value;

        private PositionedValue(Point location, Value2 value) {
            this.location = location;
            this.value = value;
        }
    }

    private ArrayList<PositionedValue> selectorValues = new ArrayList<>();
    private ArrayList<PositionedValue> behaviorValues = new ArrayList<>();

    public void addSelectorValue(Point location, Value2 value) {
        selectorValues.add(new PositionedValue(location, value));
    }

    public void addBehaviorValue(Point location, Value2 value) {
        behaviorValues.add(new PositionedValue(location, value));
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));

        JPanel selectorView = new JPanel(null);
        selectorView.setPreferredSize(new Dimension(200, 75));
        selectorView.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        JPanel behaviorView = new JPanel(null);
        behaviorView.setPreferredSize(new Dimension(200, 75));

        view.add(selectorView);
        view.add(behaviorView);

        view.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        view.setSize(view.getPreferredSize());

        selectorValues.forEach(x -> {
            JComponent component = new Value2Holder(x.value).toView(playgroundView).getComponent();
            component.setLocation(x.location);
            view.add(component);
        });

        view.addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        view.setSize(view.getPreferredSize());
                    }
                };

                e.getChild().addComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());
            }
        });

        ComponentUtil.addObserverCleanupLogic(this, view, () -> {
            view.revalidate();
            view.repaint();
        });

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return view;
            }
        };
    }

    private int parameterId;
    private ArrayList<ParameterValue> parameters = new ArrayList<>();

    @Override
    public Editor createEditor(PlaygroundView playgroundView, Point location, Value2ViewWrapper value2ViewWrapper) {
        JComponent targetComponent = (JComponent) value2ViewWrapper.getView().getComponentAt(location.x, location.y);
        Point targetLocation = SwingUtilities.convertPoint(value2ViewWrapper.getView(), location, targetComponent);
        int componentIndex = value2ViewWrapper.getView().getComponentZOrder(targetComponent);

        ParseContext parseContext = new ParseContext() {
            @Override
            public ParameterValue newParameter() {
                ParameterValue parameter = new ParameterValue(parameterId++);

                parameters.add(parameter);

                return parameter;
            }

            @Override
            public IdProvider newIdProviderForFrame() {
                return null;
            }
        };

        return new ParsingEditor(playgroundView, parseContext) {
            JComponent editorComponent;

            @Override
            public String getText() {
                return " ";
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                this.editorComponent = editorComponent;

                editorComponent.setLocation(targetLocation);
                editorComponent.setSize(200, 15);
                //editorComponent.setPreferredSize(editorComponent.getSize());
                //editorComponent.setSize(value2ViewWrapper.getView().getPreferredSize());

                //value2ViewWrapper.remove(value2ViewWrapper.getView());
                targetComponent.add(editorComponent);

                targetComponent.repaint();
                targetComponent.revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                targetComponent.remove(editorComponent);

                ScopeView scopeView = new ScopeView((ValueView)parsedComponent);
                scopeView.setLocation(editorComponent.getLocation());

                targetComponent.add(scopeView);

                targetComponent.repaint();
                targetComponent.revalidate();
            }

            @Override
            protected void endEdit(Value2 parsedValue) {
                targetComponent.remove(editorComponent);

                Value2Holder value2Holder = new Value2Holder(parsedValue);
                JComponent valueViewWrapper = value2Holder.toView(playgroundView).getComponent();// new Value2ViewWrapper(parsedValue, scopeView);

                valueViewWrapper.setLocation(editorComponent.getLocation());

                targetComponent.add(valueViewWrapper);

                targetComponent.repaint();
                targetComponent.revalidate();

                switch(componentIndex) {
                    case 0:
                        addSelectorValue(targetLocation, value2Holder);
                        break;
                    case 1:
                        addBehaviorValue(targetLocation, value2Holder);
                        break;
                }
            }

            @Override
            public void cancelEdit() {
                targetComponent.remove(editorComponent);
                //value2ViewWrapper.add(value2ViewWrapper.getView());

                targetComponent.repaint();
                targetComponent.revalidate();
            }
        };
    }

    @Override
    public void drop(PlaygroundView playgroundView, Value2ViewWrapper droppedComponent, Point location, Value2ViewWrapper value2ViewWrapper) {
        JComponent targetComponent = (JComponent) value2ViewWrapper.getView().getComponentAt(location.x, location.y);
        Point targetLocation = SwingUtilities.convertPoint(value2ViewWrapper.getView(), location, targetComponent);
        int componentIndex = value2ViewWrapper.getView().getComponentZOrder(targetComponent);

        droppedComponent.getValue().addObserver(this);
        droppedComponent.setLocation(targetLocation);
        targetComponent.add(droppedComponent);

        targetComponent.revalidate();
        targetComponent.repaint();

        ((ClassValue)value2ViewWrapper.getValue()).addSelectorValue(location, droppedComponent.getValue());

        switch(componentIndex) {
            case 0:
                ((ClassValue)value2ViewWrapper.getValue()).addSelectorValue(location, droppedComponent.getValue());
                break;
            case 1:
                ((ClassValue)value2ViewWrapper.getValue()).addBehaviorValue(location, droppedComponent.getValue());
                break;
        }
    }

    @Override
    public String getText() {
        return "{}";
    }

    @Override
    public String getSource(TextContext textContext) {
        return "{}";
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        ClassValue classValue = new ClassValue();

        selectorValues.forEach(x -> classValue.addSelectorValue(x.location, x.value.reduce(environment)));

        return classValue;
    }

    public Value2 instantiate() {
        Hashtable<String, Value2> environment = new Hashtable<>();

        ClassValue classValue = new ClassValue();

        selectorValues.forEach(x -> classValue.addSelectorValue(x.location, new ReductionValue2(x.value)));

        return classValue;
    }

    @Override
    public void updated() {
        sendUpdated();
    }
}
