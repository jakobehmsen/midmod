package paidia;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface Value2 {
    ViewBinding2 toView(PlaygroundView playgroundView);

    String getText();
    String getSource(TextContext textContext);

    Value2 reduce(Map<String, Value2> environment);

    void addObserver(Value2Observer observer);
    void removeObserver(Value2Observer observer);

    default Value2 reduce() {
        return new ReductionValue2(this);
    }

    default Value2 derive() {
        return this;
    }

    default List<String> getParameters() {
        ArrayList<String> parameters = new ArrayList<>();
        appendParameters(parameters);
        return parameters;
    }

    default void appendParameters(List<String> parameters) {

    }

    default Value2 shadowed(FrameValue frame) {
        return this;
    }

    default Editor createEditor(PlaygroundView playgroundView, Point location, Value2ViewWrapper value2ViewWrapper) {
        return new ParsingEditor(playgroundView) {
            JComponent editorComponent;

            @Override
            public String getText() {
                return value2ViewWrapper.getValue().getSource(new DefaultTextContext());
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                this.editorComponent = editorComponent;

                editorComponent.setSize(value2ViewWrapper.getView().getPreferredSize());

                value2ViewWrapper.remove(value2ViewWrapper.getView());
                value2ViewWrapper.add(editorComponent);

                value2ViewWrapper.repaint();
                value2ViewWrapper.revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                value2ViewWrapper.remove(editorComponent);

                ScopeView scopeView = new ScopeView((ValueView)parsedComponent);
                scopeView.setLocation(editorComponent.getLocation());

                value2ViewWrapper.add(scopeView);

                value2ViewWrapper.repaint();
                value2ViewWrapper.revalidate();
            }

            @Override
            protected void endEdit(Value2 parsedValue) {
                value2ViewWrapper.remove(editorComponent);

                ViewBinding2 viewBinding = parsedValue.toView(playgroundView);

                JComponent scopeView = viewBinding.getComponent();

                scopeView.setLocation(editorComponent.getLocation());

                value2ViewWrapper.setValue(parsedValue);

                //value2ViewWrapper.add(scopeView);
                //value2ViewWrapper.setView(scopeView);

                value2ViewWrapper.repaint();
                value2ViewWrapper.revalidate();
            }

            @Override
            public void cancelEdit() {
                value2ViewWrapper.remove(editorComponent);
                value2ViewWrapper.add(value2ViewWrapper.getView());

                value2ViewWrapper.repaint();
                value2ViewWrapper.revalidate();
            }
        };
    }

    default void drop(PlaygroundView playgroundView, Value2ViewWrapper droppedComponent, Point location, Value2ViewWrapper value2ViewWrapper) {
        value2ViewWrapper.setView(droppedComponent.getView());
        value2ViewWrapper.removeAll();
        value2ViewWrapper.add(droppedComponent.getView());
        value2ViewWrapper.setValue(droppedComponent.getValue());
        value2ViewWrapper.revalidate();
        value2ViewWrapper.repaint();

        /*Value2ViewWrapper targetComponentParent = (Value2ViewWrapper) Stream.iterate(value2ViewWrapper.getParent(), c -> (JComponent)c.getParent()).filter(x -> x instanceof Value2ViewWrapper).findFirst().get();
        location = SwingUtilities.convertPoint(value2ViewWrapper, location, targetComponentParent);
        targetComponentParent.drop(playgroundView, droppedComponent, location);*/
    }

    default Value2 forApplication() {
        return this;
    }

    default boolean canMove(Value2ViewWrapper parentViewWrapper, Value2ViewWrapper viewWrapper) {
        return false;
    }

    default boolean canReduceFrom() {
        return false;
    }
}
