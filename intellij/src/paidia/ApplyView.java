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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ApplyView extends JPanel implements ValueView, ValueViewContainer {
    private JComponent functionView;
    private List<Argument> arguments;

    public ApplyView(JComponent functionView, List<JComponent> arguments) {
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
            Argument argument = new Argument(name, view);

            //argument.valueView = x;
            //argument.editableView =
            //argument

            return argument;
        }).collect(Collectors.toList());

        this.arguments.forEach(x -> add(x));

        setSize(getPreferredSize());
    }

    @Override
    public String getText(TextContext textContext) {
        //FunctionView functionView = (FunctionView) ((ValueView)this.functionView).reduce();

        return IntStream.range(0, ((ValueView)this.functionView).getIdentifiers().size()).mapToObj(i -> {
            String n = ((ValueView)this.functionView).getIdentifiers().get(i);
            return n + " = " + ((ValueView)arguments.get(i).valueView).getText(textContext);
        }).collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        IntStream.range(0, arguments.size()).forEach(index -> {
            Argument x = arguments.get(index);
            x.editableView = playgroundView.createEditableView(new Editor() {
                @Override
                public String getText() {
                    return ((ValueView)x.valueView).getText(new DefaultTextContext());
                }

                @Override
                public void beginEdit(JComponent editorComponent) {
                    editorComponent.setPreferredSize(x.valueView.getPreferredSize());
                    x.beginEdit(editorComponent);
                    //remove(index);
                    //add(editorComponent, index);
                    setSize(getPreferredSize());

                    repaint();
                    revalidate();
                }

                @Override
                public void endEdit(JComponent parsedComponent) {
                    changeArgument(playgroundView, index, x, parsedComponent);
                }

                @Override
                public void cancelEdit() {
                    x.cancelEdit();
                    //remove(index);
                    //add(x.valueView, index);
                    setSize(getPreferredSize());

                    repaint();
                    revalidate();
                }
            });
            playgroundView.makeEditableByMouse(() -> x.editableView, x.valueView);
        });
    }

    private void changeArgument(PlaygroundView playgroundView, int index, Argument argument, JComponent valueView) {
        argument.changeValue(valueView);
        //remove(index);
        //add(valueView, index);
        ((ValueView)argument.valueView).removeObserver(argument.observer);
        ((ValueView)argument.valueView).release();
        argument.valueView = valueView;
        ((ValueView)argument.valueView).addObserver(argument.observer);

        playgroundView.makeEditableByMouse(() -> argument.editableView, argument.valueView);
        ((ValueView)argument.valueView).setup(playgroundView);

        setSize(getPreferredSize());

        repaint();
        revalidate();

        observers.forEach(x -> x.updated());
    }

    @Override
    public ValueView reduce(Map<String, ValueView> arguments) {
        // Ignore given arguments;
        // - Or, could see arguments as a dynamic scope?
        new String();
        Map<String, ValueView> ownArguments = IntStream.range(0, this.arguments.size()).mapToObj(i -> i)
            .collect(Collectors.toMap(i -> ((ValueView)this.functionView).getIdentifiers().get(i.intValue()), i -> (ValueView)this.arguments.get(i.intValue()).valueView));
        return ((ValueView)this.functionView).reduce(ownArguments);
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
    public EditableView getEditorFor(JComponent valueView) {
        return null;
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        return null;
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
            this.valueView = valueView;
            remove(((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER));
            add(valueView, BorderLayout.CENTER);
        }
    }

    /*private Argument createArgument(int index, JComponent valueView) {
        Argument argument = new Argument();
        argument.valueView = valueView;
        add(argument.valueView, index);
        argument.observer = new ValueViewObserver() {
            @Override
            public void updated() {
                observers.forEach(x -> x.updated());
            }
        };

        ((ValueView)argument.valueView).addObserver(argument.observer);
        return argument;
    }*/
}
