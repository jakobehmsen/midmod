package paidia;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface ViewBinding {
    JComponent getView();
    void release();
    boolean isCompatibleWith(Value value);
    void updateFrom(Value value);
    default void setupEditor(ConstructorCell editor) {

    }
    default void setupWorkspace(Workspace workspace) {

    }
    default List<ViewBinding> getViews() {
        return Collections.emptyList();
    }
    default ViewBinding findView(Point location) {
        Optional<ViewBinding> foundView = getViews().stream()
            .sorted((x, y) -> x.getView().getParent().getComponentZOrder(x.getView()) - y.getView().getParent().getComponentZOrder(y.getView()))
            .filter(x -> x.getView().getBounds().contains(location))
            .findFirst();

        if(foundView.isPresent()) {
            location.translate(-foundView.get().getView().getX(), -foundView.get().getView().getY());
            return foundView.get().findView(location);
        } else {
            return this;
        }
    }

    default boolean canDrop() {
        return false;
    }

    default void drop(Value value, Point location) {

    }
}
