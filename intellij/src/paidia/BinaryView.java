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
        add(new JLabel(operator.getFormatted()), 1);
        rhs = createArgument(2, rhsView);

        //setBorder(BorderFactory.createRaisedSoftBevelBorder());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        setSize(getPreferredSize());
    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        setupArgument(playgroundView, 0, lhs);
        setupArgument(playgroundView, 2, rhs);
    }

    @Override
    public ValueView reduce(Map<String, ValueView> arguments) {
        return reducer.apply(new ValueView[]{((ValueView)lhs.valueView).reduce(arguments), ((ValueView)rhs.valueView).reduce(arguments)});
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
        argument.editableView = playgroundView.createEditableView(new ParsingEditor() {
            @Override
            public String getText() {
                return ((ValueView)argument.valueView).getText(new DefaultTextContext());
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
        playgroundView.makeEditableByMouse(() -> argument.editableView, argument.valueView);
        ((ValueView)argument.valueView).setup(playgroundView);
    }

    @Override
    public String getText(TextContext textContext) {
        String text = ((ValueView)lhs.valueView).getText(textOperator) + operator.getRaw() + ((ValueView)rhs.valueView).getText(textOperator);

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

        playgroundView.makeEditableByMouse(() -> argument.editableView, argument.valueView);
        ((ValueView)argument.valueView).setup(playgroundView);

        setSize(getPreferredSize());

        repaint();
        revalidate();

        observers.forEach(x -> x.updated());
    }

    @Override
    public void appendIdentifiers(List<String> identifiers) {
        ((ValueView)lhs.valueView).appendIdentifiers(identifiers);
        ((ValueView)rhs.valueView).appendIdentifiers(identifiers);
    }
}
