package paidia;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

public class ReductionView extends JLabel implements ValueView {
    private JComponent valueView;
    private ValueView reduction;
    private ValueViewObserver observer;

    public ReductionView(JComponent valueView) {
        // TODO: Should be replaced by ApplyView? Is simply, implicitly an ApplyView with no arguments?
        this.valueView = valueView;
        reduction = ((ValueView)valueView).evaluate(new Hashtable<>());
        setFont(new Font(getFont().getFamily(), Font.ITALIC | Font.BOLD, getFont().getSize()));
        setText(reduction.getText());

        observer = new ValueViewObserver() {
            @Override
            public void updated() {
                reduction = ((ValueView)valueView).evaluate(new Hashtable<>());
                setText(reduction.getText());
                observers.forEach(x -> x.updated());
                setSize(getPreferredSize());
            }
        };

        ((ValueView)valueView).addObserver(observer);
        setSize(getPreferredSize());

    }

    public void setValueView(JComponent valueView) {
        ((ValueView)this.valueView).removeObserver(observer);

        this.valueView = valueView;
        reduction = ((ValueView)valueView).evaluate(Collections.emptyMap());
        setText(reduction.getSource(new DefaultTextContext()));

        observer = new ValueViewObserver() {
            @Override
            public void updated() {
                reduction = ((ValueView)valueView).evaluate(Collections.emptyMap());
                setText(reduction.getSource(new DefaultTextContext()));
                observers.forEach(x -> x.updated());
                setSize(getPreferredSize());
            }
        };

        ((ValueView)valueView).addObserver(observer);

        observers.forEach(x -> x.updated());
        setSize(getPreferredSize());
    }

    @Override
    public String getSource(TextContext textContext) {
        return reduction.getSource(textContext);
    }

    @Override
    public void setup(PlaygroundView playgroundView) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return reduction;
    }

    private ArrayList<ValueViewObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(ValueViewObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ValueViewObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void release() {
        ((ValueView)valueView).removeObserver(observer);
    }
}
