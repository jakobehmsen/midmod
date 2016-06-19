package paidia;

import javax.swing.*;
import java.util.Map;

public class IfView extends CompositeValueView {
    public IfView(ValueView condition, ValueView trueExpression, ValueView falseExpression) {
        addChild(condition);
        addChild(trueExpression);
        addChild(falseExpression);
    }

    @Override
    public String getText(TextContext textContext) {
        TextContext ifTextContext = new DefaultTextContext();
        return "if " + getChild(0).getText(ifTextContext) +
            " then " + getChild(1).getText(ifTextContext) +
            " else " + getChild(2).getText(ifTextContext);
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        boolean conditionResult = (boolean)((AtomView)getChild(0).evaluate(environment)).getValue();

        return conditionResult ? getChild(1).evaluate(environment) : getChild(2).evaluate(environment);
    }

    @Override
    protected void addChildAsComponent(int index, JComponent child) {
        switch (index) {
            case 0:
                add(new JLabel("if"), 0);
                add(child, 1);
                break;
            case 1:
                add(new JLabel("then"), 2);
                add(child, 3);
                break;
            case 2:
                add(new JLabel("else"), 4);
                add(child, 5);
                break;
        }
    }

    @Override
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
    }
}
