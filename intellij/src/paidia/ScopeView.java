package paidia;

import javax.swing.*;
import java.util.Map;

public class ScopeView extends CompositeValueView implements ValueViewContainer {
    public ScopeView(ValueView valueView) {
        addChild(valueView);
    }

    @Override
    public String getText(TextContext textContext) {
        return getChild(0).getText(textContext);
    }

    @Override
    public void setText(String text) {

    }

    public void setValueView(JComponent valueView) {
        setChild(0, (ValueView)valueView);
    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        getChild(0).setup(playgroundView);
    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return getChild(0).evaluate(environment);
    }
}
