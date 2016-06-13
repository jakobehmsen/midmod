package paidia;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ValueView {
    String getText(TextContext textContext);

    void setText(String text);

    void setup(PlaygroundView playgroundView);

    ValueView reduce(Map<String, ValueView> arguments);

    void addObserver(ValueViewObserver observer);
    void removeObserver(ValueViewObserver observer);

    void release();

    default List<String> getIdentifiers() {
        ArrayList<String> identifiers = new ArrayList<>();
        appendIdentifiers(identifiers);
        return identifiers;
    }

    default void appendIdentifiers(List<String> identifiers) {

    }
}
