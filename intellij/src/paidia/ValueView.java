package paidia;

import javax.swing.*;
import java.awt.*;

public interface ValueView {
    String getText(TextContext textContext);

    void setText(String text);

    void setup(PlaygroundView playgroundView);

    ValueView reduce();

    void addObserver(ValueViewObserver observer);
    void removeObserver(ValueViewObserver observer);

    void release();

    default void drop(JComponent dropped, JComponent target) {

    }
}
