package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class CompositeValueView extends JPanel implements ValueView, ValueViewContainer {
    protected interface ValueViewChildI {
        // Could support editables?
        ValueView getChild();
        void setChild(ValueView valueView);
        ValueViewObserver getObserver();
    }

    private static class ValueViewChild {
        ValueView child;
        ValueViewObserver observer;
        EditableView editableView;
    }

    private ArrayList<ValueViewChild> children = new ArrayList<>();
    private boolean isUpdating;

    public CompositeValueView() {
        addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        if(isUpdating)
                            return;

                        setSize(getPreferredSize());

                        revalidate();
                        repaint();
                    }
                };

                e.getChild().addComponentListener(componentAdapter);

                if(isUpdating)
                    return;

                setSize(getPreferredSize());

                revalidate();
                repaint();
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);

                if(isUpdating)
                    return;

                setSize(getPreferredSize());

                revalidate();
                repaint();
            }
        });

        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    protected void beginUpdate() {
        isUpdating = true;
    }

    protected void endUpdate() {
        isUpdating = false;

        setSize(getPreferredSize());

        revalidate();
        repaint();
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

        if(playgroundView != null) {
            valueViewChild.child.setup(playgroundView);
            playgroundView.makeEditableByMouse((JComponent) valueViewChild.child);
        }

        addChildAsComponent(children.size() - 1, (JComponent) child);

        sendUpdated();
    }

    protected ValueView getChild(int index) {
        return children.get(index).child;
    }

    protected void setChild(int index, ValueView child) {
        ValueViewChild valueViewChild = children.get(index);
        ValueView childOld = valueViewChild.child;

        if(playgroundView != null)
            playgroundView.unmakeEditableByMouse((JComponent) valueViewChild.child);

        valueViewChild.child.removeObserver(valueViewChild.observer);
        valueViewChild.child.release();
        valueViewChild.child = child;
        valueViewChild.child.addObserver(valueViewChild.observer);

        if(playgroundView != null) {
            valueViewChild.child.setup(playgroundView);
            playgroundView.makeEditableByMouse((JComponent) valueViewChild.child);
        }

        setChildAsComponent(index, (JComponent) childOld, (JComponent) valueViewChild.child);

        sendUpdated();
    }

    protected void removeChild(int index) {
        ValueViewChild valueViewChild = children.get(index);

        valueViewChild.child.removeObserver(valueViewChild.observer);
        valueViewChild.child.release();

        children.remove(index);

        if(playgroundView != null)
            playgroundView.unmakeEditableByMouse((JComponent) valueViewChild.child);

        valueViewChild.child.release();

        removeChildAsComponent(children.size(), (JComponent) valueViewChild.child);

        sendUpdated();
    }

    protected void addChildAsComponent(int index, JComponent child) {
        add(child, index);
    }

    protected void removeChildAsComponent(int index, JComponent child) {
        remove(index);
    }

    protected void setChildAsComponent(int index, JComponent childOld, JComponent childNew) {
        removeChildAsComponent(index, childOld);
        addChildAsComponent(index, childNew);
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

    private PlaygroundView playgroundView;

    @Override
    public void setup(PlaygroundView playgroundView) {
        this.playgroundView = playgroundView;

        getChildren().forEach(x -> {
            x.setup(playgroundView);
            playgroundView.makeEditableByMouse((JComponent) x);
        });
    }

    @Override
    public void release() {
        getChildren().forEach(x -> {
            playgroundView.unmakeEditableByMouse((JComponent) x);
            x.release();
        });
    }

    protected EditableView createChildEditableView(PlaygroundView playgroundView, ValueViewChild child) {
        return playgroundView.createEditableView(new ParsingEditor() {
            private int childZOrder;

            @Override
            public String getText() {
                return (child.child.getText(new DefaultTextContext()));
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                editorComponent.setPreferredSize(((JComponent)child.child).getPreferredSize());
                //childZOrder = getComponentZOrder((Component) child.child);
                childZOrder = zOrderFromChild((JComponent) child.child);
                remove(childZOrder);
                add(editorComponent, childZOrder);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                int index = getChildren().indexOf(child.child);
                setChild(index, (ValueView) parsedComponent);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void cancelEdit() {
                remove(childZOrder);
                add((Component) child.child, childZOrder);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }
        });
    }

    protected int zOrderFromChild(JComponent child) {
        return getComponentZOrder(child);
    }

    @Override
    public EditableView getEditorFor(JComponent valueView) {
        int index = IntStream.range(0, children.size()).filter(i -> children.get(i).child == valueView).findFirst().getAsInt();
        if(children.get(index).editableView == null)
            children.get(index).editableView = createChildEditableView(playgroundView, children.get(index));
        return children.get(index).editableView;
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        return null;
    }
}
