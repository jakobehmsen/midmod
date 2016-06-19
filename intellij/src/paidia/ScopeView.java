package paidia;

import java.util.Map;

public class ScopeView extends CompositeValueView {
    public ScopeView(ValueView valueView) {
        addChild(valueView);

        setSize(getPreferredSize());

        //setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));

        setBorder(new RoundedBorder());
    }

    @Override
    public String getSource(TextContext textContext) {
        return getChild(0).getSource(textContext);
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return getChild(0).evaluate(environment);
    }
}
