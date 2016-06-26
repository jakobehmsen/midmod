package paidia;

import javax.swing.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Hashtable;
import java.util.Map;

public class ReductionValue2 extends AbstractValue2 implements Value2Observer {
    private Value2 value;
    private Value2 reduction;

    public ReductionValue2(Value2 value) {
        this.value = value;
        updated();
        value.addObserver(this);
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JLabel label = new JLabel(reduction.getText());

        label.addHierarchyListener(new HierarchyListener() {
            Value2Observer observer = () ->
                label.setText(reduction.getText());

            boolean lastDisplayable;

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if(e.getChanged() == label && label.isDisplayable() != lastDisplayable) {
                    if(!lastDisplayable) {
                        addObserver(observer);
                    } else {
                        removeObserver(observer);
                    }

                    lastDisplayable = label.isDisplayable();
                }
            }
        });

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return label;
            }
        };
    }

    @Override
    public String getText() {
        return value.getText();
    }

    @Override
    public String getSource(TextContext textContext) {
        return value.getSource(textContext);
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return reduction;
    }

    @Override
    public void updated() {
        reduction = value.reduce(new Hashtable<>());
        sendUpdated();
    }
}
