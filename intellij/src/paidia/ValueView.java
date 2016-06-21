package paidia;

import java.util.*;

public interface ValueView {
    default String getSummaryText() {
        return getText();
    }

    default String getText() {
        return getSource(new DefaultTextContext());
    }

    String getSource(TextContext textContext);

    void setText(String text);

    void setup(PlaygroundView playgroundView);

    ValueView evaluate(Map<String, ValueView> environment);

    void addObserver(ValueViewObserver observer);
    void removeObserver(ValueViewObserver observer);

    void release();

    default List<String> getIdentifiers() {
        ArrayList<String> identifiers = new ArrayList<>();
        appendIdentifiers(new HashSet<>(), identifiers);
        return identifiers;
    }

    default void appendIdentifiers(Set<String> locals, List<String> identifiers) {

    }

    default ValueView passAsReference() {
        return evaluate(new Hashtable<>());
    }
}
