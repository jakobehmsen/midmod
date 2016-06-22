package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ApplyView extends JPanel implements ValueView, ValueViewContainer {
    private JComponent functionView;
    private List<Argument> arguments;
    private ValueViewObserver observer;

    public ApplyView(JComponent functionView, List<JComponent> arguments) {
        ((FlowLayout)getLayout()).setHgap(5);
        ((FlowLayout)getLayout()).setVgap(0);

        addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        setSize(getPreferredSize());
                    }
                };

                e.getChild().addComponentListener(componentAdapter);
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
            }
        });

        this.functionView = functionView;
        this.arguments = IntStream.range(0, arguments.size()).mapToObj(i -> {
            String name = ((ValueView)this.functionView).getIdentifiers().get(i);
            JComponent view = arguments.get(i);
            return createArgument(name, view);
        }).collect(Collectors.toList());

        this.arguments.forEach(x -> add(x));

        setSize(getPreferredSize());

        setBackground(Color.WHITE);
        //setBorder(BorderFactory.createDashedBorder(Color.GRAY, 4.0f, 4.0f));
        setForeground(Color.GREEN);
        //setBorder(new RoundedBorder()); // TODO: Derive arc according to distance to ScopeView

        setOpaque(false);
    }

    private Argument createArgument(String name, JComponent view) {
        Argument argument = new Argument(name, view);

        argument.observer = new ValueViewObserver() {
            @Override
            public void updated() {
                observers.forEach(x -> x.updated());
            }
        };

        return argument;
    }

    private void update(PlaygroundView playgroundView) {
        List<String> addedParameters = ((ValueView)ApplyView.this.functionView).getIdentifiers().stream()
            .filter(x -> !ApplyView.this.arguments.stream().anyMatch(y -> y.name.equals(x)))
            .collect(Collectors.toList());

        List<String> removedParameters = ApplyView.this.arguments.stream().map(x -> x.name)
            .filter(x -> !((ValueView)ApplyView.this.functionView).getIdentifiers().contains(x))
            .collect(Collectors.toList());

        removedParameters.forEach(x -> {
            int index = IntStream.range(0, arguments.size()).filter(i -> arguments.get(i).name.equals(x)).findFirst().getAsInt();
            Argument argument = arguments.get(index);
            releaseArgument(argument);
            arguments.remove(index);
            remove(argument);

            setSize(getPreferredSize());
            revalidate();
            repaint();
        });

        addedParameters.forEach(x -> {
            int indexOfArgument = ((ValueView)ApplyView.this.functionView).getIdentifiers().indexOf(x);
            Argument argument = createArgument(x, playgroundView.createDefaultValueView());
            arguments.add(indexOfArgument, argument);
            add(argument, indexOfArgument);
            setupArgument(playgroundView, argument);

            setSize(getPreferredSize());
            revalidate();
            repaint();
        });

        observers.forEach(x -> x.updated());
    }

    @Override
    public String getSource(TextContext textContext) {
        return IntStream.range(0, ((ValueView)this.functionView).getIdentifiers().size()).mapToObj(i -> {
            String n = ((ValueView)this.functionView).getIdentifiers().get(i);
            return n + " = " + ((ValueView)arguments.get(i).valueView).getSource(textContext);
        }).collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        // playgroundView could be derived looking into the ancestry
        observer = new ValueViewObserver() {
            @Override
            public void updated() {
                update(playgroundView);
            }
        };

        ((ValueView)functionView).addObserver(observer);

        IntStream.range(0, arguments.size()).forEach(index -> {
            Argument x = arguments.get(index);
            setupArgument(playgroundView, x);
        });

        JComponent closestParentValueView = (JComponent) Stream.iterate(getParent(), c -> c.getParent()).filter(x -> x instanceof ValueView).findFirst().get();
        if(!(closestParentValueView instanceof ScopeView)) {
            setBorder(new RoundedBorder());
            ((RoundedBorder) getBorder()).adjustArcSize(this);
        } else {
            closestParentValueView.setForeground(Color.GREEN);
        }
    }

    private void setupArgument(PlaygroundView playgroundView, Argument x) {
        ((ValueView)x.valueView).addObserver(x.observer);
        x.editableView = playgroundView.createEditableView(new ParsingEditor(playgroundView) {
            @Override
            public String getText() {
                return ((ValueView)x.valueView).getSource(new DefaultTextContext());
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                editorComponent.setPreferredSize(x.valueView.getPreferredSize());
                x.beginEdit(editorComponent);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                changeArgument(playgroundView, x, parsedComponent);
            }

            @Override
            public void cancelEdit() {
                x.cancelEdit();
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }
        });
        playgroundView.makeEditableByMouse(x.valueView);
    }

    private void releaseArgument(Argument argument) {
        ((ValueView)argument.valueView).removeObserver(argument.observer);
        ((ValueView)argument.valueView).release();
    }

    private void changeArgument(PlaygroundView playgroundView, Argument argument, JComponent valueView) {
        argument.changeValue(valueView);
        releaseArgument(argument);
        argument.valueView = valueView;
        ((ValueView)argument.valueView).addObserver(argument.observer);

        playgroundView.makeEditableByMouse(argument.valueView);
        ((ValueView)argument.valueView).setup(playgroundView);

        setSize(getPreferredSize());

        repaint();
        revalidate();

        observers.forEach(x -> x.updated());
    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        Map<String, ValueView> applyEnvironment = IntStream.range(0, this.arguments.size()).mapToObj(i -> i)
            .collect(Collectors.toMap(
                i -> ((ValueView)this.functionView).getIdentifiers().get(i.intValue()),
                i -> ((ValueView)this.arguments.get(i.intValue()).valueView).evaluate(environment)));

        return ((ValueView)this.functionView).evaluate(applyEnvironment);
    }

    private ArrayList<ValueViewObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(ValueViewObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ValueViewObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void release() {

    }

    @Override
    public void appendIdentifiers(Set<String> locals, List<String> identifiers) {
        arguments.forEach(x -> ((ValueView)x.valueView).appendIdentifiers(locals, identifiers));
    }

    @Override
    public EditableView getEditorFor(JComponent valueView) {
        return null;
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        return null;
    }

    public void setValueView(PlaygroundView playgroundView, JComponent valueView) {
        ((ValueView)this.functionView).removeObserver(observer);

        this.functionView = valueView;

        ((ValueView)valueView).addObserver(observer);

        update(playgroundView);
    }

    private static class Argument extends JPanel implements ValueViewContainer {
        private String name;
        private JComponent valueView;
        private EditableView editableView;
        private ValueViewObserver observer;

        public Argument(String name, JComponent valueView) {
            this.name = name;
            this.valueView = valueView;

            addContainerListener(new ContainerAdapter() {
                ComponentAdapter componentAdapter;

                @Override
                public void componentAdded(ContainerEvent e) {
                    componentAdapter = new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                            setSize(getPreferredSize());
                        }
                    };

                    e.getChild().addComponentListener(componentAdapter);
                }

                @Override
                public void componentRemoved(ContainerEvent e) {
                    e.getChild().removeComponentListener(componentAdapter);
                }
            });

            setLayout(new BorderLayout());
            add(new JLabel(name + "="), BorderLayout.WEST);
            add(valueView, BorderLayout.CENTER);

            setSize(getPreferredSize());
            setOpaque(false);
        }

        @Override
        public EditableView getEditorFor(JComponent valueView) {
            return editableView;
        }

        @Override
        public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
            return null;
        }

        public void beginEdit(JComponent editorComponent) {
            remove(valueView);
            add(editorComponent, BorderLayout.CENTER);
        }

        public void cancelEdit() {
            remove(((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER));
            add(valueView, BorderLayout.CENTER);
        }

        public void changeValue(JComponent valueView) {
            remove(((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER));
            add(valueView, BorderLayout.CENTER);
        }

        @Override
        public void drop(PlaygroundView playgroundView, JComponent dropped, JComponent target) {
            if(target == valueView) {
                ((ApplyView)getParent()).changeArgument(playgroundView, this, dropped);
            }
        }
    }
}
