package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

public class Value2ViewWrapper extends JPanel {
    private PlaygroundView playgroundView;
    private ValueHolderInterface value2Holder;
    private JComponent view;
    private Value2 value;

    public Value2ViewWrapper(PlaygroundView playgroundView, ValueHolderInterface value2Holder, JComponent view) {
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

        ComponentUtil.addObserverCleanupLogic(value2Holder, this, new Value2Observer() {
            @Override
            public void updated(Change change) {
                if(change instanceof ValueHolderInterface.HeldValueChange) {
                    removeAll();
                    JComponent valueView = getValue().toView(playgroundView).getComponent();
                    add(valueView);
                    setView(valueView);
                    revalidate();
                    repaint();
                } else if(change instanceof ValueHolderInterface.MetaValueChange) {
                    ValueHolderInterface.MetaValueChange metaValueChange = (ValueHolderInterface.MetaValueChange) change;
                    if(metaValueChange.getId().equals("Location")) {
                        if (!endingMove) {
                            Value2ViewWrapper.this.setLocation((Point) getValueHolder().getMetaValue("Location"));
                        }
                    } else if(metaValueChange.getId().equals("Size")) {
                        if (!endingMove) {
                            Value2ViewWrapper.this.getView().setSize((Dimension) getValueHolder().getMetaValue("Size"));
                            Value2ViewWrapper.this.getView().setPreferredSize((Dimension) getValueHolder().getMetaValue("Size"));
                        }
                    }
                }/* else if(change instanceof ValueHolderInterface.HeldLocationChange) {
                    if(!endingMove) {
                        Value2ViewWrapper.this.setLocation(getValueHolder().getLocation());
                    }
                }*/
            }
        });

        if(value2Holder.getMetaValue("Size") != null) {
            getView().setSize((Dimension) value2Holder.getMetaValue("Size"));
            getView().setPreferredSize((Dimension) value2Holder.getMetaValue("Size"));
        }
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

    public void drop(PlaygroundView playgroundView, Value2ViewWrapper sourceDroppedValue, Value2 droppedValue, Point location) {
        getValue().drop(playgroundView, sourceDroppedValue, droppedValue, location, this);
    }

    public void startMove() {

    }

    private boolean endingMove;

    public void endMove() {
        endingMove = true;
        //getValueHolder().setLocation(getLocation());
        getValueHolder().setMetaValue("Location", getLocation());
        getValueHolder().setMetaValue("Size", getSize());
        endingMove = false;
    }
}
