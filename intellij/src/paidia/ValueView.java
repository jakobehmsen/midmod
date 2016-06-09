package paidia;

import javax.swing.*;

public interface ValueView {
    String getText(TextContext textContext);

    void setText(String text);

    void setup(PlaygroundView playgroundView);

    ValueView reduce();

    void addObserver(ValueViewObserver observer);
    void removeObserver(ValueViewObserver observer);

    void release();

    default void drop(PlaygroundView playgroundView, JComponent dropped, JComponent target) {

    }
}
