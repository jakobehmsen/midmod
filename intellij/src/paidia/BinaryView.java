package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class BinaryView extends JPanel implements ValueView, ValueViewContainer {
    private Text operator;
    private TextContext textOperator;
    private Argument lhs;
    private Argument rhs;
    private Function<ValueView[], ValueView> reducer;

    public BinaryView(Text operator, TextContext textOperator, JComponent lhsView, JComponent rhsView, Function<ValueView[], ValueView> reducer) {
        this.operator = operator;
        this.textOperator = textOperator;
        this.reducer = reducer;
        ((FlowLayout)getLayout()).setHgap(0);
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

        lhs = createArgument(0, lhsView);
        JLabel operatorLabel = new JLabel(operator.getFormatted());
        operatorLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        add(operatorLabel, 1);
        rhs = createArgument(2, rhsView);

        //setBorder(BorderFactory.createRaisedSoftBevelBorder());
        setBackground(Color.WHITE);
        setForeground(new Color(220, 220, 220));
        //setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        setSize(getPreferredSize());

        setAlignmentX(Component.LEFT_ALIGNMENT);
        setOpaque(false);
        //setAlignmentY(Component.TOP_ALIGNMENT);
    }

    @Override
    public Insets getInsets() {
        return super.getInsets();
    }

    @Override
    public Dimension getPreferredSize() {
        return super.getPreferredSize();
    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        setupArgument(playgroundView, 0, lhs);
        setupArgument(playgroundView, 2, rhs);

        JComponent closestParentValueView = (JComponent) Stream.iterate(getParent(), c -> c.getParent()).filter(x -> x instanceof ValueView).findFirst().get();
        if(!(closestParentValueView instanceof ScopeView)) {
            setBorder(new RoundedBorder());
            ((RoundedBorder) getBorder()).adjustArcSize(this);
        }
    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return reducer.apply(new ValueView[]{((ValueView)lhs.valueView).evaluate(environment), ((ValueView)rhs.valueView).evaluate(environment)});
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
        int index = getComponentZOrder(valueView);
        Argument argument = index == 0 ? lhs : rhs;
        return argument.editableView;
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        int index = getComponentZOrder(valueView);
        Argument argument = index == 0 ? lhs : rhs;
        return new ChildSlot() {
            @Override
            public void replace(JComponent view) {
                view.setPreferredSize(argument.valueView.getPreferredSize());
                remove(index);
                add(view, index);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void revert() {
                remove(index);
                add(argument.valueView, index);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void commit(JComponent valueView) {
                changeArgument(playgroundView, index, argument, valueView);
            }
        };
    }

    private static class Argument {
        private JComponent valueView;
        private EditableView editableView;
        private ValueViewObserver observer;
    }

    private Argument createArgument(int index, JComponent valueView) {
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
    }

    private void setupArgument(PlaygroundView playgroundView, int index, Argument argument) {
        argument.editableView = playgroundView.createEditableView(new ParsingEditor(playgroundView) {
            @Override
            public String getText() {
                return ((ValueView)argument.valueView).getSource(new DefaultTextContext());
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                editorComponent.setPreferredSize(argument.valueView.getPreferredSize());
                remove(index);
                add(editorComponent, index);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                changeArgument(playgroundView, index, argument, parsedComponent);
            }

            @Override
            public void cancelEdit() {
                remove(index);
                add(argument.valueView, index);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }
        });
        playgroundView.makeEditableByMouse(argument.valueView);
        ((ValueView)argument.valueView).setup(playgroundView);
    }

    @Override
    public String getSource(TextContext textContext) {
        String text = ((ValueView)lhs.valueView).getSource(textOperator) + operator.getRaw() + ((ValueView)rhs.valueView).getSource(textOperator);

        return textOperator.getText(textContext, text);
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public void drop(PlaygroundView playgroundView, JComponent dropped, JComponent target) {
        int index = getComponentZOrder(target);
        Argument argument = index == 0 ? lhs : rhs;

        changeArgument(playgroundView, index, argument, dropped);
    }

    private void changeArgument(PlaygroundView playgroundView, int index, Argument argument, JComponent valueView) {
        remove(index);
        add(valueView, index);
        ((ValueView)argument.valueView).removeObserver(argument.observer);
        ((ValueView)argument.valueView).release();
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
    public void appendIdentifiers(Set<String> locals, List<String> identifiers) {
        ((ValueView)lhs.valueView).appendIdentifiers(locals, identifiers);
        ((ValueView)rhs.valueView).appendIdentifiers(locals, identifiers);
    }
}
