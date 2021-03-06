package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ReductionValue2 extends AbstractValue2 implements Value2Observer {
    private Value2 value;
    private Value2 reduction;

    public ReductionValue2(Value2 value) {
        this.value = value;
        updated(new Change(this));
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

        ((FlowLayout)view.getLayout()).setHgap(0);
        ((FlowLayout)view.getLayout()).setVgap(0);

        view.add(reduction.toView(playgroundView).getComponent());
        //JLabel label = new JLabel(reduction.getText());

        ComponentUtil.addObserverCleanupLogic(this, view, (Change change) -> {
            view.removeAll();
            view.add(reduction.toView(playgroundView).getComponent());
            view.revalidate();
            view.repaint();
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
    public void updated(Change change) {
        reduction = value.reduce(new Hashtable<>());
        sendUpdated(change);
    }

    @Override
    public Value2 shadowed(java.util.List<FrameValue> frames) {
        return new ReductionValue2(value.shadowed(frames));
    }
}
