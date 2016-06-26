package paidia;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplyValue2 extends AbstractValue2 implements Value2Observer {
    private Value2 value;
    private Hashtable<String, Value2> arguments;

    public ApplyValue2(Value2 value) {
        this.value = value;
        value.addObserver(this);
        arguments = new Hashtable<>();
    }

    public void setArgument(String name, Value2 value) {
        Value2 currentValue = arguments.get(name);
        if(currentValue != null)
            currentValue.removeObserver(this);
        if(value != null) {
            arguments.put(name, value);
            value.addObserver(this);
        } else {
            arguments.remove(name);
        }
        // Why aren't ApplyValue2 updated when an argument is changed?
        sendUpdated();
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JPanel view = new JPanel();

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

        arguments.entrySet().forEach(x -> {
            JPanel argument = new JPanel();

            argument.addContainerListener(new ContainerAdapter() {
                ComponentAdapter componentAdapter;

                @Override
                public void componentAdded(ContainerEvent e) {
                    componentAdapter = new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            argument.setSize(argument.getPreferredSize());
                        }
                    };

                    e.getChild().addComponentListener(componentAdapter);
                    argument.setSize(argument.getPreferredSize());
                }

                @Override
                public void componentRemoved(ContainerEvent e) {
                    e.getChild().removeComponentListener(componentAdapter);
                    argument.setSize(argument.getPreferredSize());
                }
            });

            argument.add(new JLabel(x.getKey() + " = "));
            argument.add(x.getValue().toView(playgroundView).getComponent());

            view.add(argument);
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
        return arguments.entrySet().stream().map(x -> x.getKey() + " = " + x.getValue().getText()).collect(Collectors.joining(", "));
    }

    @Override
    public String getSource(TextContext textContext) {
        return arguments.entrySet().stream().map(x -> x.getKey() + " = " + x.getValue().getSource(textContext)).collect(Collectors.joining(", "));
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return value.reduce(arguments.entrySet().stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().reduce(environment))));
    }

    @Override
    public void appendParameters(List<String> parameters) {
        arguments.values().forEach(x -> x.appendParameters(parameters));
    }

    @Override
    public void updated() {
        // Did parameters change?

        sendUpdated();
    }
}
