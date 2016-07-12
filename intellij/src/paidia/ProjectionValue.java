package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.Map;
import java.util.function.Function;

public class ProjectionValue extends AbstractValue2 implements Value2Observer {
    private Value2 source;
    private Value2 projectedValue;
    private Function<Value2, Value2> projector;

    public ProjectionValue(Value2 source, Function<Value2, Value2> projector) {
        this.source = source;
        this.projector = projector;
        projectedValue = projector.apply(source);
        source.addObserver(this);
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

        view.add(projectedValue.toView(playgroundView).getComponent());

        ComponentUtil.addObserverCleanupLogic(this, view, (Change change) -> {
            view.removeAll();
            view.add(projectedValue.toView(playgroundView).getComponent());
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
        return source.getText();
    }

    @Override
    public String getSource(TextContext textContext) {
        return source.getSource(textContext);
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return projectedValue;
    }

    @Override
    public void updated(Change change) {
        if(change instanceof Value2Holder.HeldValueChange) {
            // Release current projectedValue
            //projectedValue.release();
            projectedValue = projector.apply(source);
        }

        sendUpdated(change);
    }

    @Override
    public Value2 shadowed(FrameValue frame) {
        return new ReductionValue2(source.shadowed(frame));
    }
}
