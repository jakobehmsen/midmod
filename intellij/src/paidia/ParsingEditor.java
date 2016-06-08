package paidia;

import javax.swing.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class ParsingEditor implements Editor {
    /*@Override
    public void endEdit(String text) {
        JComponent parsedComponent = ComponentParser.parseComponent(text);
        endEdit(parsedComponent);
        listeners.forEach(x -> x.accept((ValueView)parsedComponent));
    }*/

    //protected abstract void endEdit(JComponent parsedComponent);

    /*private ArrayList<Consumer<ValueView>> listeners = new ArrayList<>();

    public void addChangeListener(Consumer<ValueView> listener) {
        listeners.add(listener);
    }*/
}
