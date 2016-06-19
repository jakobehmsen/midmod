package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.Map;
import java.util.stream.IntStream;

public class IfView extends CompositeValueView {
    public IfView(ValueView condition, ValueView trueExpression, ValueView falseExpression) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        addChild(condition);
        addChild(trueExpression);
        addChild(falseExpression);
    }

    @Override
    public String getSource(TextContext textContext) {
        TextContext ifTextContext = new DefaultTextContext();
        return "if " + getChild(0).getSource(ifTextContext) +
            " then " + getChild(1).getSource(ifTextContext) +
            " else " + getChild(2).getSource(ifTextContext);
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        int zOrder = zOrderFromChild(valueView);
        JComponent valueViewAsBlock = (JComponent) getComponent(zOrder);

        return new ChildSlot() {
            @Override
            public void replace(JComponent view) {
                valueViewAsBlock.remove(2);
                valueViewAsBlock.add(view, 2);
            }

            @Override
            public void revert() {
                replace(valueView);
            }

            @Override
            public void commit(JComponent valueView) {
                replace(valueView);
            }
        };
    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        boolean conditionResult = (boolean)((AtomView)getChild(0).evaluate(environment)).getValue();

        return conditionResult ? getChild(1).evaluate(environment) : getChild(2).evaluate(environment);
    }

    private JComponent createKeywordComponent(String text) {
        JLabel keywordComponent = new JLabel(text);

        keywordComponent.setForeground(Color.BLUE);

        return keywordComponent;
    }

    @Override
    protected void addChildAsComponent(int index, JComponent child) {
        Box b = Box.createHorizontalBox();

        b.addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        b.setSize(b.getPreferredSize());
                    }
                };

                e.getChild().addComponentListener(componentAdapter);
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
            }
        });

        JComponent keywordComponent = null;

        switch (index) {
            case 0:
                keywordComponent = createKeywordComponent("if");
                break;
            case 1:
                keywordComponent = createKeywordComponent("then");
                break;
            case 2:
                keywordComponent = createKeywordComponent("else");
                break;
        }

        b.add(keywordComponent);
        b.add(Box.createHorizontalStrut(5));
        b.add(child);
        b.add(Box.createHorizontalGlue());
        super.addChildAsComponent(index, b);

        /*switch (index) {
            case 0:
                add(createKeywordComponent("if"), 0);
                add(child, 1);
                break;
            case 1:
                add(createKeywordComponent("then"), 2);
                add(child, 3);
                break;
            case 2:
                add(createKeywordComponent("else"), 4);
                add(child, 5);
                break;
        }*/
    }

    @Override
    protected void setChildComponent(JComponent childBeingEdited, JComponent replacement) {
        super.setChildComponent(childBeingEdited, replacement);
    }

    @Override
    protected int zOrderFromChild(JComponent child) {
        return IntStream.range(0, getComponentCount()).filter(x ->
            ((Box)getComponent(x)).getComponent(2) == child).findFirst().getAsInt();
    }

    /*@Override
    protected void removeChildAsComponent(int index, JComponent child) {
        switch (index) {
            case 0:
                remove(0);
                remove(0);
                break;
            case 1:
                remove(2);
                remove(2);
                break;
            case 2:
                remove(4);
                remove(4);
                break;
        }
    }*/
}
