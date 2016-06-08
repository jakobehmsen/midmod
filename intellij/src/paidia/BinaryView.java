package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

public class BinaryView extends JPanel implements ValueView {
    private Text operator;
    private TextContext textOperator;
    private Argument lhs;
    private Argument rhs;

    public BinaryView(Text operator, TextContext textOperator, JComponent lhsView, JComponent rhsView) {
        this.operator = operator;
        this.textOperator = textOperator;

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

    private static class Argument {
        private JComponent valueView;
        private EditableView editableView;
    }


    private Argument createArgument(int index, JComponent valueView) {
        Argument argument = new Argument();
        argument.valueView = valueView;
        add(argument.valueView, index);
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
            protected void endEdit(JComponent parsedComponent) {
                remove(index);
                add(parsedComponent, index);
                argument.valueView = parsedComponent;
                playgroundView.makeEditableByMouse(() -> argument.editableView, argument.valueView);
                ((ValueView)argument.valueView).setup(playgroundView);
                setSize(getPreferredSize());

                repaint();
                revalidate();
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
}
