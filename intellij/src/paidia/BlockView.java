package paidia;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockView extends CompositeValueView {
    public BlockView(List<ValueView> expressions) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        expressions.forEach(x -> {
            addChild(x);
        });
    }

    @Override
    public String getText(TextContext textContext) {
        return getChildren().stream().map(x -> x.getText(textContext)).collect(Collectors.joining("\n"));
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        for(int i = 0; i < getChildren().size() - 1; i++)
            getChildren().get(i).evaluate(environment);

        return getChildren().get(getChildren().size() - 1);
    }
}
