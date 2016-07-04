package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

public class Value2ViewWrapper extends JPanel {
    private ValueHolderInterface value2Holder;
    private JComponent view;
    private Value2 value;

    public Value2ViewWrapper(ValueHolderInterface value2Holder, JComponent view) {
        this.value2Holder = value2Holder;
        this.view = view;

        ((FlowLayout)getLayout()).setHgap(0);
        ((FlowLayout)getLayout()).setVgap(0);

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
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
                setSize(getPreferredSize());
            }
        });

        add(view);
    }

    public Value2 getValue() {
        return value2Holder.getValue();
    }

    public void beginEdit(PlaygroundView playgroundView, Point location) {
        Editor editor = getValue().createEditor(playgroundView, location, this);

        playgroundView.createEditableView(editor).beginEdit();
    }

    public ValueHolderInterface getValueHolder() {
        return value2Holder;
    }

    public JComponent getView() {
        return view;
    }

    public void setValue(Value2 value) {
        value2Holder.setValue(value);
    }

    public void setView(JComponent view) {
        this.view = view;
    }

    public void drop(PlaygroundView playgroundView, Value2ViewWrapper droppedComponent, Point location) {
        getValue().drop(playgroundView, droppedComponent, location, this);
    }
}
