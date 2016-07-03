package paidia;

import javax.swing.*;
import java.awt.event.*;
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
        JPanel view = new JPanel();

        view.addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        view.setSize(view.getPreferredSize());
                    }
                };

                e.getChild().addComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());
            }
        });

        view.add(value.toView(playgroundView).getComponent());
        //JLabel label = new JLabel(reduction.getText());

        ComponentUtil.addObserverCleanupLogic(this, view, () -> {
            view.removeAll();
            view.add(value.toView(playgroundView).getComponent());
        });

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return view;
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
