package paidia;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockView extends CompositeValueView implements ValueViewContainer {
    public BlockView(List<ValueView> expressions) {
        expressions.forEach(x -> {
            addChild(x);
            add((JComponent)x);
        });

        // TODO: Should layout vertically
        setLayout(new FlowLayout(FlowLayout.TRAILING));
    }

    @Override
    public String getText(TextContext textContext) {
        return getChildren().stream().map(x -> x.getText(textContext)).collect(Collectors.joining("\n"));
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        getChildren().forEach(x -> x.setup(playgroundView));
        getChildren().forEach(x -> playgroundView.makeEditableByMouse((JComponent) x));
    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        for(int i = 0; i < getChildren().size() - 1; i++)
            getChildren().get(i).evaluate(environment);

        return getChildren().get(getChildren().size() - 1);
    }

    @Override
    public void release() {
        getChildren().forEach(x -> x.release());
    }

    @Override
    public EditableView getEditorFor(JComponent valueView) {
        // TODO: How to hold editors for each valueView?
        // Could composite value view be extended to support this?
        return null;
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        return null;
    }
}
