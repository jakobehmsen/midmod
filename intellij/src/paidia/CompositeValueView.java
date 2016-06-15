package paidia;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CompositeValueView extends JPanel implements ValueView {
    private static class ValueViewChild {
        ValueView child;
        ValueViewObserver observer;
    }

    private ArrayList<ValueViewChild> children = new ArrayList<>();

    public CompositeValueView() {
        addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        setSize(getPreferredSize());
                    }
                };

                e.getChild().addComponentListener(componentAdapter);

                setSize(getPreferredSize());

                revalidate();
                repaint();
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);

                setSize(getPreferredSize());

                revalidate();
                repaint();
            }
        });
    }

    protected void addChild(ValueView child) {
        ValueViewChild valueViewChild = new ValueViewChild();

        valueViewChild.child = child;
        valueViewChild.observer = new ValueViewObserver() {
            @Override
            public void updated() {
                sendUpdated();
            }
        };

        child.addObserver(valueViewChild.observer);

        children.add(valueViewChild);

        sendUpdated();
    }

    protected void setChild(int index, ValueView child) {
        ValueViewChild valueViewChild = children.get(index);

        valueViewChild.child.removeObserver(valueViewChild.observer);
        valueViewChild.child.release();
        valueViewChild.child = child;
        valueViewChild.child.addObserver(valueViewChild.observer);

        sendUpdated();
    }

    protected void removeChild(int index) {
        ValueViewChild valueViewChild = children.get(index);

        valueViewChild.child.removeObserver(valueViewChild.observer);
        valueViewChild.child.release();

        children.remove(index);

        sendUpdated();
    }

    protected List<ValueView> getChildren() {
        return children.stream().map(x -> x.child).collect(Collectors.toList());
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

    protected void sendUpdated() {
        observers.forEach(x -> x.updated());
    }

    @Override
    public void appendIdentifiers(List<String> identifiers) {
        children.forEach(x -> x.child.appendIdentifiers(identifiers));
    }
}
