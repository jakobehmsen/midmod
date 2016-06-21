package paidia;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class ReferenceView  extends JLabel implements ValueView {
    private JComponent valueView;
    private ValueViewObserver observer;

    public ReferenceView(JComponent valueView) {
        // TODO: Should be replaced by ApplyView? Is simply, implicitly an ApplyView with no arguments?
        this.valueView = valueView;
        setFont(new Font(getFont().getFamily(), Font.ITALIC | Font.BOLD, getFont().getSize()));
        setText(((ValueView)valueView).getText());

        observer = new ValueViewObserver() {
            @Override
            public void updated() {
                setText(((ValueView)valueView).getText());
                observers.forEach(x -> x.updated());
                setSize(getPreferredSize());
            }
        };

        ((ValueView)valueView).addObserver(observer);
        setSize(getPreferredSize());

    }

    @Override
    public String getSource(TextContext textContext) {
        return ((ValueView)valueView).getSource(textContext);
    }

    @Override
    public void setup(PlaygroundView playgroundView) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return (ValueView)valueView;
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
