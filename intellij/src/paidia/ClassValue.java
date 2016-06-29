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
import java.util.stream.Collectors;

public class ClassValue extends AbstractValue2 implements Value2Observer {
    private static class PositionedValue {
        private Point location;
        private Value2 value;

        private PositionedValue(Point location, Value2 value) {
            this.location = location;
            this.value = value;
        }
    }

    private ArrayList<PositionedValue> values = new ArrayList<>();

    public void addValue(Point location, Value2 value) {
        values.add(new PositionedValue(location, value));
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JPanel view = new JPanel(null);

        view.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        view.setPreferredSize(new Dimension(200, 150));
        view.setSize(view.getPreferredSize());

        values.forEach(x -> {
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

    @Override
    public Editor createEditor(PlaygroundView playgroundView, Point location, Value2ViewWrapper value2ViewWrapper) {
        return new ParsingEditor(playgroundView) {
            JComponent editorComponent;

            @Override
            public String getText() {
                return " ";
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                this.editorComponent = editorComponent;

                editorComponent.setLocation(location);
                editorComponent.setSize(200, 15);
                //editorComponent.setPreferredSize(editorComponent.getSize());
                //editorComponent.setSize(value2ViewWrapper.getView().getPreferredSize());

                //value2ViewWrapper.remove(value2ViewWrapper.getView());
                value2ViewWrapper.getView().add(editorComponent);

                value2ViewWrapper.repaint();
                value2ViewWrapper.revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                value2ViewWrapper.getView().remove(editorComponent);

                ScopeView scopeView = new ScopeView((ValueView)parsedComponent);
                scopeView.setLocation(editorComponent.getLocation());

                value2ViewWrapper.getView().add(scopeView);

                value2ViewWrapper.repaint();
                value2ViewWrapper.revalidate();
            }

            @Override
            protected void endEdit(Value2 parsedValue) {
                value2ViewWrapper.getView().remove(editorComponent);

                ViewBinding2 viewBinding = parsedValue.toView(playgroundView);

                JComponent scopeView = viewBinding.getComponent();
                Value2Holder value2Holder = new Value2Holder(parsedValue);
                JComponent valueViewWrapper = value2Holder.toView(playgroundView).getComponent();// new Value2ViewWrapper(parsedValue, scopeView);

                valueViewWrapper.setLocation(editorComponent.getLocation());

                value2ViewWrapper.getView().add(valueViewWrapper);

                value2ViewWrapper.repaint();
                value2ViewWrapper.revalidate();

                addValue(location, value2Holder);
            }

            @Override
            public void cancelEdit() {
                value2ViewWrapper.getView().remove(editorComponent);
                //value2ViewWrapper.add(value2ViewWrapper.getView());

                value2ViewWrapper.repaint();
                value2ViewWrapper.revalidate();
            }
        };
    }

    @Override
    public void drop(PlaygroundView playgroundView, Value2ViewWrapper droppedComponent, Point location, Value2ViewWrapper value2ViewWrapper) {
        droppedComponent.getValue().addObserver(this);
        droppedComponent.setLocation(location);
        value2ViewWrapper.getView().add(droppedComponent);

        value2ViewWrapper.getView().revalidate();
        value2ViewWrapper.getView().repaint();

        ((ClassValue)value2ViewWrapper.getValue()).addValue(location, droppedComponent.getValue());

        addValue(location, droppedComponent.getValue());
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

        values.forEach(x -> classValue.addValue(x.location, x.value.reduce(environment)));

        return classValue;
    }

    public Value2 instantiate() {
        Hashtable<String, Value2> environment = new Hashtable<>();

        ClassValue classValue = new ClassValue();

        values.forEach(x -> classValue.addValue(x.location, new ReductionValue2(x.value)));

        return classValue;
    }

    @Override
    public void updated() {
        sendUpdated();
    }
}
