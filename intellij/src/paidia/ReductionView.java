package paidia;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ReductionView extends JLabel implements ValueView {
    private JComponent valueView;
    private ValueView reduction;
    private ValueViewObserver observer;

    public ReductionView(JComponent valueView) {
        this.valueView = valueView;
        reduction = ((ValueView)valueView).reduce();
        setFont(new Font(getFont().getFamily(), Font.ITALIC | Font.BOLD, getFont().getSize()));
        setText(reduction.getText(new DefaultTextContext()));

        observer = new ValueViewObserver() {
            @Override
            public void updated() {
                reduction = ((ValueView)valueView).reduce();
                setText(reduction.getText(new DefaultTextContext()));
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
        reduction = ((ValueView)valueView).reduce();
        setText(reduction.getText(new DefaultTextContext()));

        observer = new ValueViewObserver() {
            @Override
            public void updated() {
                reduction = ((ValueView)valueView).reduce();
                setText(reduction.getText(new DefaultTextContext()));
                observers.forEach(x -> x.updated());
                setSize(getPreferredSize());
            }
        };

        ((ValueView)valueView).addObserver(observer);

        observers.forEach(x -> x.updated());
        setSize(getPreferredSize());
    }

    @Override
    public String getText(TextContext textContext) {
        return getText();
    }

    @Override
    public void setup(PlaygroundView playgroundView) {

    }

    @Override
    public ValueView reduce() {
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
