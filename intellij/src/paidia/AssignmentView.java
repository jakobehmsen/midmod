package paidia;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AssignmentView extends CompositeValueView {
    private String id;

    public AssignmentView(String id, ValueView value) {
        this.id = id;

        add(new JLabel(id + " = "));
        addChild(value);
    }

    private boolean hasValue() {
        return getChildCount() == 1;
    }

    private ValueView getValue() {
        return getChild(0);
    }

    @Override
    public String getText(TextContext textContext) {
        return id + " = " + getValue().getText(textContext);
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        ValueView valueResult = hasValue() ? getValue().evaluate(environment) : null;

        environment.put(id, valueResult);

        return valueResult;
    }

    @Override
    public void appendIdentifiers(Set<String> locals, List<String> identifiers) {
        locals.add(id);
        super.appendIdentifiers(locals, identifiers);
    }

    @Override
    protected void addChildAsComponent(int index, JComponent child) {
        super.addChildAsComponent(1, child);
    }

    @Override
    protected void removeChildAsComponent(int index, JComponent child) {
        super.removeChildAsComponent(1, child);
    }
}
