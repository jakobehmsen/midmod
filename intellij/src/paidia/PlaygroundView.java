package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PlaygroundView extends JPanel {
    private EditableView currentEditableView;

    private MouseTool currentMouseTool;
    private JPopupMenu mouseToolSelector;
    private MouseAdapter currentMouseToolWrapper;

    private Action createMouseToolSelector(String text, MouseTool mouseTool) {
        return new AbstractAction(text) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentMouseTool != null)
                    currentMouseTool.endTool(PlaygroundView.this);
                currentMouseTool = mouseTool;
                String title = ((JFrame) SwingUtilities.getWindowAncestor(PlaygroundView.this)).getTitle().split(" - ")[0];
                ((JFrame) SwingUtilities.getWindowAncestor(PlaygroundView.this)).setTitle(title + " - " + text);
                currentMouseTool.startTool(PlaygroundView.this);
            }
        };
    }

    public PlaygroundView() {
        setLayout(null);

        currentMouseTool = new MouseTool() {
            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Please right click and select a tool");
            }
        };

        currentMouseTool.startTool(this);

        mouseToolSelector = new JPopupMenu();
        mouseToolSelector.setOpaque(false);
        mouseToolSelector.setForeground(Color.BLACK);
        mouseToolSelector.setBorder(new RoundedBorder(25, new Insets(5, 5, 5, 5)));
        mouseToolSelector.add(createMouseToolSelector("Write", createWriteMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Move", createMoveMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Resize", createResizeMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Reduce", createReduceMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Derive", createDeriveMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Delete", createDeleteMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Name", createNameMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Apply", createApplyMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Reference", createReferenceMouseTool()));

        // What if each mouse button could be a tool reference, that can be changed on the run?
        // - Then, which one should be used for mouse-over/enter/exit events?
        //this.setComponentPopupMenu(mouseToolSelector);

        ComponentAdapter componentAdapter = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                e.getComponent().revalidate();
                e.getComponent().repaint();
            }
        };

        addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                revalidate();
                repaint(e.getChild().getBounds());

                e.getChild().addComponentListener(componentAdapter);

                if(e.getChild() instanceof ValueView) {
                    ((ValueView) e.getChild()).setup(PlaygroundView.this);
                    makeEditableByMouse((JComponent) e.getChild());
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                revalidate();
                repaint(e.getChild().getBounds());

                e.getChild().removeComponentListener(componentAdapter);

                if(e.getChild() instanceof ValueView) {
                    ((Container)e.getChild()).removeContainerListener(this);
                    ((ValueView)e.getChild()).release();
                }
            }
        });

        currentMouseToolWrapper = createWrapperForMouseTool();
        addMouseListener(currentMouseToolWrapper);
        addMouseMotionListener(currentMouseToolWrapper);
    }

    private MouseAdapter createWrapperForMouseTool() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                currentMouseTool.mouseClicked(e);
            }

            boolean isPopupTrigger;

            @Override
            public void mousePressed(MouseEvent e) {
                currentMouseTool.mousePressed(e);

                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), PlaygroundView.this);

                if(e.isPopupTrigger()) {
                    isPopupTrigger = true;

                    mouseToolSelector.show(PlaygroundView.this, p.x, p.y);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentMouseTool.mouseReleased(e);

                if(isPopupTrigger) {
                    mouseToolSelector.setVisible(false);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                currentMouseTool.mouseDragged(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if(e.getComponent() != PlaygroundView.this)
                    currentMouseTool.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if(e.getComponent() != PlaygroundView.this)
                    currentMouseTool.mouseExited(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //if(e.getComponent() != PlaygroundView.this)
                    currentMouseTool.mouseMoved(e);
            }
        };
    }

    private MouseTool createWriteMouseTool() {

        /*
        What if editing simply begins when typing somewhere based on where the cursor was when the first key was typed?
        What if editing is implicitly committed when editing an existing expression and starting to edit somewhere else
        or, more generally, just losing focus? - I.e., "cancel" is caused by loss of focus.
        */
        return new MouseTool() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                    PlaygroundView.this.setToolTipText("");

                    EditableView editableView;

                    if(e.getComponent() == PlaygroundView.this) {

                        editableView = createRootEditableView(() -> "", editorComponent -> {
                            editorComponent.setLocation(e.getPoint());
                            editorComponent.setSize(80, 15);
                        }, newValueView -> {
                        }, () -> {
                        });

                        editableView.beginEdit();
                    } else {
                        ((Value2ViewWrapper)e.getComponent()).beginEdit(PlaygroundView.this, e.getPoint());
                        //ValueViewContainer container = (ValueViewContainer) Stream.iterate(e.getComponent().getParent(), c -> (JComponent)c.getParent()).filter(x -> x instanceof ValueViewContainer).findFirst().get();
                        //editableView = container.getEditorFor((JComponent) e.getComponent());
                    }
                    //editableView.beginEdit();
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Click to create or edit an arithmetic expression - and hit enter to do the change.");
            }

            @Override
            public void endTool(JComponent component) {
                if(currentEditableView != null) {
                    currentEditableView.commitEdit();
                    //currentEditableView.cancelEdit();
                }
            }
        };
    }

    private MouseTool createMoveMouseTool() {
        return new MouseTool() {
            private JComponent targetValueView;
            private int mousePressX;
            private int mousePressY;
            private boolean moving;

            private boolean canMove(JComponent x) {
                if(x.getParent() == PlaygroundView.this)
                    return true;

                if(x instanceof Value2ViewWrapper) {
                    Value2ViewWrapper parentViewWrapper = nearestValue2ViewWrapper((JComponent) x.getParent());
                    return parentViewWrapper.getValue().canMove(parentViewWrapper, (Value2ViewWrapper)x);
                }

                return false;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> canMove(x)).findFirst().get();

                    moving = true;
                    targetValueView.getParent().setComponentZOrder(targetValueView, 0);

                    int cursorType = Cursor.MOVE_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    mousePressX = e.getX();
                    mousePressY = e.getY();

                    ((Value2ViewWrapper)targetValueView).startMove();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(moving) {
                    PlaygroundView.this.setToolTipText("");

                    int deltaX = e.getX() - mousePressX;
                    int deltaY = e.getY() - mousePressY;

                    targetValueView.setLocation(targetValueView.getX() + deltaX, targetValueView.getY() + deltaY);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && moving) {
                    moving = false;
                    int cursorType = Cursor.DEFAULT_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    ((Value2ViewWrapper)targetValueView).endMove();
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Press and drag an object to move it.");
            }
        };
    }

    private static final int NORTH = 0;
    private static final int SOUTH = 1;
    private static final int WEST = 0;
    private static final int EAST = 1;
    private static final int CENTER = 2;

    private MouseTool createResizeMouseTool() {
        return new MouseTool() {
            private int hLocation;
            private int vLocation;
            private int marginSize = 5;

            private JComponent targetValueView;
            private int mousePressX;
            private int mousePressY;
            private boolean moving;

            private boolean canResize(Container x) {
                if(x.getParent() == PlaygroundView.this)
                    return true;

                if(x instanceof Value2ViewWrapper) {
                    Value2ViewWrapper parentViewWrapper = nearestValue2ViewWrapper((JComponent) x.getParent());
                    return parentViewWrapper.getValue().canMove(parentViewWrapper, (Value2ViewWrapper)x);
                }

                return false;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();

                Container targetValueView = Stream.iterate((Container)valueView, c -> c.getParent()).filter(x -> x == PlaygroundView.this || canResize(x)).findFirst().get();

                if(targetValueView == PlaygroundView.this) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                }

                Point locationInTarget = SwingUtilities.convertPoint(valueView, e.getPoint(), targetValueView);

                if(locationInTarget.x < marginSize)
                    hLocation = WEST;
                else if(locationInTarget.x >= targetValueView.getWidth() - marginSize)
                    hLocation = EAST;
                else
                    hLocation = CENTER;

                if(locationInTarget.y < marginSize)
                    vLocation = NORTH;
                else if(locationInTarget.y >= targetValueView.getHeight() - marginSize)
                    vLocation = SOUTH;
                else
                    vLocation = CENTER;

                switch(hLocation) {
                    case WEST:
                        switch(vLocation) {
                            case NORTH:
                                setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                                break;
                            case CENTER:
                                setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                                break;
                            case SOUTH:
                                setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                                break;
                        }
                        break;
                    case CENTER:
                        switch(vLocation) {
                            case NORTH:
                                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                                break;
                            case CENTER:
                                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                                break;
                            case SOUTH:
                                setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                                break;
                        }
                        break;
                    case EAST:
                        switch(vLocation) {
                            case NORTH:
                                setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                                break;
                            case CENTER:
                                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                                break;
                            case SOUTH:
                                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                                break;
                        }
                        break;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> canResize(x)).findFirst().get();

                    moving = true;
                    targetValueView.getParent().setComponentZOrder(targetValueView, 0);

                    int cursorType = Cursor.MOVE_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(getCursor());
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    mousePressX = e.getX();
                    mousePressY = e.getY();

                    ((Value2ViewWrapper)targetValueView).startMove();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(moving) {
                    PlaygroundView.this.setToolTipText("");

                    int deltaX = 0;
                    int deltaY = 0;
                    int deltaWidth = 0;
                    int deltaHeight = 0;

                    if(hLocation == CENTER && vLocation == CENTER) {
                        deltaX = e.getX() - mousePressX;
                        deltaY = e.getY() - mousePressY;
                    } else {
                        switch(vLocation) {
                            case NORTH:
                                deltaY = e.getY() - mousePressY;
                                deltaHeight = deltaY * -1;

                                break;
                            case SOUTH:
                                deltaHeight = e.getY() - targetValueView.getHeight();

                                break;
                        }

                        switch(hLocation) {
                            case WEST:
                                deltaX = e.getX() - mousePressX;
                                deltaWidth = deltaX * -1;

                                break;
                            case EAST:
                                deltaWidth = e.getX() - targetValueView.getWidth();

                                break;
                        }
                    }

                    targetValueView.setLocation(targetValueView.getX() + deltaX, targetValueView.getY() + deltaY);
                    ((Value2ViewWrapper)targetValueView).getView().setSize(targetValueView.getWidth() + deltaWidth, targetValueView.getHeight() + deltaHeight);
                    ((Value2ViewWrapper)targetValueView).getView().setPreferredSize(((Value2ViewWrapper)targetValueView).getView().getSize());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && moving) {
                    moving = false;
                    int cursorType = Cursor.DEFAULT_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    ((Value2ViewWrapper)targetValueView).endMove();
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Press and drag an object to resize it.");
            }

            @Override
            public void endTool(JComponent component) {
                super.endTool(component);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };
    }

    private Value2ViewWrapper nearestValue2ViewWrapper(JComponent component) {
        Container container = Stream.iterate((Container)component,
            c -> c.getParent())
            .filter(x -> x instanceof Value2ViewWrapper || x instanceof JFrame).findFirst().get();

        return container instanceof Value2ViewWrapper ? (Value2ViewWrapper) container : null;
    }

    private MouseTool createReduceMouseTool() {
        return new MouseTool() {
            private JComponent selection;
            private boolean linking;
            private JComponent targetValueView;
            private Color foreground;

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    linking = true;

                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x ->
                        x.getParent() == PlaygroundView.this
                        || nearestValue2ViewWrapper((JComponent) x.getParent()).getValue().canReduceFrom())
                        .findFirst().get();

                    foreground = targetValueView.getForeground();
                    targetValueView.setForeground(Color.BLUE);

                    int cursorType = Cursor.HAND_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(linking) {
                    PlaygroundView.this.setToolTipText("");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && linking) {
                    linking = false;
                    int cursorType = Cursor.DEFAULT_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    targetValueView.setForeground(foreground);

                    repaint(targetValueView.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), PlaygroundView.this);
                    JComponent targetComponent = (JComponent) findComponentAt(pointInContentPane);
                    //if(targetComponent != targetValueView) {
                        /*
                        Changes should be passed when values are updated.
                        E.g. when value holder changes its held values, a "HeldValueChange" should be send out.
                             and any values from the held value should be forwarded.
                        This way, frames should could be derived from the proxied via value holders.
                        I.e., via forwarded changes such add "AddedSlot".
                        */
                        Value2 reduction = ((Value2ViewWrapper)e.getComponent()).getValueHolder().reduce();
                        //ReductionValue2 reduction = new ReductionValue2(((Value2ViewWrapper)targetValueView).getValueHolder());
                        //Value2 reduction =
                        //Value2ViewWrapper reductionView = (Value2ViewWrapper) new Value2Holder(reduction).toView(PlaygroundView.this).getComponent();

                        if(targetComponent == PlaygroundView.this) {
                            Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                            Value2ViewWrapper reductionView = (Value2ViewWrapper) new Value2Holder(reduction).toView(PlaygroundView.this).getComponent();
                            reductionView.setLocation(pointInTargetComponent);
                            add(reductionView);
                        } else {
                            targetComponent = Stream.iterate(targetComponent, c -> (JComponent)c.getParent()).filter(x -> x instanceof Value2ViewWrapper).findFirst().get();
                            Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                            // Find nearest Value2ViewWrapper
                            Value2ViewWrapper targetComponentParent = (Value2ViewWrapper) Stream.iterate(targetComponent, c -> (JComponent)c.getParent()).filter(x -> x instanceof Value2ViewWrapper).findFirst().get();
                            // TODO: Should be ValueViewContainer instead of ValueView
                            targetComponentParent.drop(PlaygroundView.this, (Value2ViewWrapper)e.getComponent(), reduction, pointInTargetComponent);
                        }
                    //}
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Press and drag an object to reduce it as an expression.");
            }
        };
    }

    private MouseTool createDeriveMouseTool() {
        return new MouseTool() {
            private JComponent selection;
            private boolean linking;
            private JComponent targetValueView;
            private Color foreground;

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    linking = true;

                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

                    foreground = targetValueView.getForeground();
                    targetValueView.setForeground(Color.BLUE);

                    int cursorType = Cursor.HAND_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(linking) {
                    PlaygroundView.this.setToolTipText("");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && linking) {
                    linking = false;
                    int cursorType = Cursor.DEFAULT_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    targetValueView.setForeground(foreground);

                    repaint(targetValueView.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), PlaygroundView.this);
                    JComponent targetComponent = (JComponent) findComponentAt(pointInContentPane);
                    Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                    if(targetComponent != targetValueView) {
                        Value2 derivation = ((Value2ViewWrapper)e.getComponent()).getValueHolder().derive();
                        //Value2 derivation = ((Value2ViewWrapper)e.getComponent()).getValue().derive();
                        /*ProjectionValue projectionValue = new ProjectionValue(((Value2ViewWrapper)e.getComponent()).getValueHolder(), v -> {
                            return ((Value2HolderInterface)v).getValue().derive();
                        });*/
                        //Value2ViewWrapper reductionView = (Value2ViewWrapper) new Value2Holder(derivation).toView(PlaygroundView.this).getComponent();

                        if(targetComponent == PlaygroundView.this) {
                            Value2Holder derivationHolder = new Value2Holder(derivation);
                            // Location changes should be kept local?
                            ((Value2ViewWrapper)e.getComponent()).getValueHolder().addObserver(new Value2Observer() {
                                @Override
                                public void updated(Change change) {
                                    if(change instanceof ValueHolderInterface.MetaValueChange) {
                                        String mvid = ((ValueHolderInterface.MetaValueChange)change).getId();
                                        derivationHolder.setMetaValue(mvid, ((Value2ViewWrapper)e.getComponent()).getValueHolder().getMetaValue(mvid));
                                    }
                                }
                            });
                            ((Value2ViewWrapper)e.getComponent()).getValueHolder().getMetaValueIds().forEach(mvid ->
                                derivationHolder.setMetaValue(mvid, ((Value2ViewWrapper)e.getComponent()).getValueHolder().getMetaValue(mvid)));

                            Value2ViewWrapper reductionView = (Value2ViewWrapper) derivationHolder.toView(PlaygroundView.this).getComponent();
                            reductionView.setLocation(pointInTargetComponent);
                            add(reductionView);
                        } else {
                            // Find nearest Value2ViewWrapper
                            Value2ViewWrapper targetComponentParent = (Value2ViewWrapper) Stream.iterate(targetComponent, c -> (JComponent)c.getParent()).filter(x -> x instanceof Value2ViewWrapper).findFirst().get();
                            // TODO: Should be ValueViewContainer instead of ValueView
                            // Attach observer, as above, for the created value holder;
                            //  For frames, that is for the new slot
                            targetComponentParent.drop(PlaygroundView.this, (Value2ViewWrapper)e.getComponent(), derivation, pointInTargetComponent);
                        }

                        /*if(((Value2ViewWrapper)e.getComponent()).getValue() instanceof ClassValue) {
                            Value2 instance = ((ClassValue)((Value2ViewWrapper)e.getComponent()).getValue()).instantiate();
                            Value2ViewWrapper reductionView = (Value2ViewWrapper) new Value2Holder(instance).toView(PlaygroundView.this).getComponent();

                            if(targetComponent == PlaygroundView.this) {
                                reductionView.setLocation(pointInTargetComponent);
                                add(reductionView);
                            } else {
                                // Find nearest Value2ViewWrapper
                                Value2ViewWrapper targetComponentParent = (Value2ViewWrapper) Stream.iterate(targetComponent, c -> (JComponent)c.getParent()).filter(x -> x instanceof Value2ViewWrapper).findFirst().get();
                                // TODO: Should be ValueViewContainer instead of ValueView
                                targetComponentParent.drop(PlaygroundView.this, reductionView, pointInTargetComponent);
                            }
                        }*/
                    }
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Press and drag an object to reduce it as an expression.");
            }
        };
    }

    private MouseTool createDeleteMouseTool() {
        return new MouseTool() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PlaygroundView.this.setToolTipText("");

                JComponent valueView = (JComponent) e.getComponent();
                valueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

                remove(valueView);

                int cursorType = Cursor.DEFAULT_CURSOR;
                Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                int cursorType = Cursor.CROSSHAIR_CURSOR;
                Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                int cursorType = Cursor.DEFAULT_CURSOR;
                Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Click on an object to delete it.");
            }
        };
    }

    private MouseTool createNameMouseTool() {
        return new MouseTool() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PlaygroundView.this.setToolTipText("");

                JComponent valueViewTmp = (JComponent) e.getComponent();
                ScopeView valueView = (ScopeView) Stream.iterate(valueViewTmp, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

                valueView.beginEditName();
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Click on an object to give it a name.");
            }
        };
    }

    private MouseTool createApplyMouseTool() {
        return new MouseTool() {
            private JComponent selection;
            private boolean linking;
            private JComponent functionView;
            private Color foreground;

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    linking = true;

                    functionView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

                    foreground = functionView.getForeground();
                    functionView.setForeground(Color.BLUE);

                    int cursorType = Cursor.HAND_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(linking) {
                    PlaygroundView.this.setToolTipText("");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();

                if(e.getButton() == MouseEvent.BUTTON1 && linking) {
                    linking = false;
                    int cursorType = Cursor.DEFAULT_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    functionView.setForeground(foreground);
                    repaint(functionView.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(valueView, e.getPoint(), PlaygroundView.this);
                    JComponent targetComponent = (JComponent) findComponentAt(pointInContentPane);
                    Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                    if(targetComponent != valueView) {
                        if(!(((Value2ViewWrapper)functionView).getValue() instanceof ClassValue)) {
                            ApplyValue2 applyValue = new ApplyValue2(((Value2ViewWrapper) functionView).getValueHolder(), () -> new Value2Holder(new AtomValue2("0", "0", new BigDecimal(0))));
                            ((Value2ViewWrapper) functionView).getValueHolder().getParameters().forEach(x ->
                                applyValue.setArgument(x, new Value2Holder(new AtomValue2("0", "0", new BigDecimal(0)))));

                            JComponent applyView = new Value2Holder(applyValue).toView(PlaygroundView.this).getComponent();

                            if (targetComponent == PlaygroundView.this) {
                                //ScopeView scopeView = new ScopeView(applyView);
                                applyView.setLocation(pointInTargetComponent);
                                add(applyView);
                            } else {
                                JComponent targetComponentParent = (JComponent) targetComponent.getParent();
                                ((ValueViewContainer) targetComponentParent).drop(PlaygroundView.this, applyView, targetComponent);
                            }
                        } else {
                            ApplyClassValue2 applyValue = new ApplyClassValue2((ClassValue) ((Value2ViewWrapper) functionView).getValueHolder().getValue(), () -> new Value2Holder(new AtomValue2("0", "0", new BigDecimal(0))));
                            //((Value2ViewWrapper) functionView).getValueHolder().getParameters().forEach(x ->
                            //    applyValue.setArgument(x, new Value2Holder(new AtomValue2("0", "0", new BigDecimal(0)))));

                            JComponent applyView = new Value2Holder(applyValue).toView(PlaygroundView.this).getComponent();

                            if (targetComponent == PlaygroundView.this) {
                                //ScopeView scopeView = new ScopeView(applyView);
                                applyView.setLocation(pointInTargetComponent);
                                add(applyView);
                            } else {
                                JComponent targetComponentParent = (JComponent) targetComponent.getParent();
                                ((ValueViewContainer) targetComponentParent).drop(PlaygroundView.this, applyView, targetComponent);
                            }
                        }

                        /*ApplyView applyView = new ApplyView(functionView, ((ValueView)functionView).getIdentifiers().stream().map(x -> createDefaultValueView()).collect(Collectors.toList()));

                        if(targetComponent == PlaygroundView.this) {
                            ScopeView scopeView = new ScopeView(applyView);
                            scopeView.setLocation(pointInTargetComponent);
                            add(scopeView);
                        } else {
                            JComponent targetComponentParent = (JComponent) targetComponent.getParent();
                            ((ValueViewContainer) targetComponentParent).drop(PlaygroundView.this, applyView, targetComponent);
                        }*/
                    }
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Press and drag a function to apply it.");
            }
        };
    }

    private MouseTool createReferenceMouseTool() {
        return new MouseTool() {
            private JComponent selection;
            private boolean linking;
            private JComponent targetValueView;
            private Color foreground;

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    linking = true;

                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

                    foreground = targetValueView.getForeground();
                    targetValueView.setForeground(Color.BLUE);

                    int cursorType = Cursor.HAND_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(linking) {
                    PlaygroundView.this.setToolTipText("");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && linking) {
                    linking = false;
                    int cursorType = Cursor.DEFAULT_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    targetValueView.setForeground(foreground);
                    repaint(targetValueView.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(targetValueView, e.getPoint(), PlaygroundView.this);
                    JComponent targetComponent = (JComponent) findComponentAt(pointInContentPane);
                    Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                    if(targetComponent != targetValueView) {
                        ReferenceView reduction = new ReferenceView(targetValueView);

                        if(targetComponent == PlaygroundView.this) {
                            ScopeView scopeView = new ScopeView(reduction);
                            scopeView.setLocation(pointInTargetComponent);
                            add(scopeView);
                        } else {
                            JComponent targetComponentParent = (JComponent) targetComponent.getParent();
                            // TODO: Should be ValueViewContainer instead of ValueView
                            ((ValueViewContainer) targetComponentParent).drop(PlaygroundView.this, reduction, targetComponent);
                        }
                    }
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Press and drag an object to reduce it as an expression.");
            }
        };
    }

    public JComponent createDefaultValueView() {
        return new AtomView("0", new BigDecimal(0));
    }

    public EditableView createRootEditableView(Supplier<String> textSupplier, Consumer<JComponent> beginEdit, Consumer<JComponent> endEdit, Runnable cancelEdit) {
        return createEditableView(new ParsingEditor(this) {
            JComponent editorComponent;

            @Override
            public String getText() {
                return textSupplier.get();
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                this.editorComponent = editorComponent;

                beginEdit.accept(editorComponent);
                add(editorComponent);

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                remove(editorComponent);

                ScopeView scopeView = new ScopeView((ValueView)parsedComponent);
                scopeView.setLocation(editorComponent.getLocation());

                add(scopeView);

                endEdit.accept(parsedComponent);

                repaint();
                revalidate();
            }

            @Override
            protected void endEdit(Value2 parsedValue) {
                remove(editorComponent);

                JComponent valueViewWrapper = new Value2Holder(parsedValue).toView(PlaygroundView.this).getComponent();

                valueViewWrapper.setLocation(editorComponent.getLocation());

                add(valueViewWrapper);

                endEdit.accept(valueViewWrapper);

                repaint();
                revalidate();
            }

            @Override
            public void cancelEdit() {
                remove(editorComponent);
                cancelEdit.run();

                repaint();
                revalidate();
            }
        });
    }

    public EditableView createEditableView(Editor editor) {
        EditableView[] editableViews = new EditableView[1];

        editableViews[0] = new EditableView(new TextParser() {
            @Override
            public JComponent parse(JComponent editorComponent, String text) {
                return ComponentParser.parseComponent(text, PlaygroundView.this);
            }
        }, new Editor() {
            @Override
            public String getText() {
                return editor.getText();
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                if(currentEditableView != null) {
                    try {
                        currentEditableView.commitEdit();
                    } catch(Exception e) {
                        currentEditableView.cancelEdit();
                    }
                    //currentEditableView.cancelEdit();
                }

                editor.beginEdit(editorComponent);

                currentEditableView = editableViews[0];

                editorComponent.requestFocusInWindow();
            }

            @Override
            public void endEdit(String text) {
                editor.endEdit(text);

                currentEditableView = null;
            }

            /*@Override
            public void endEdit(JComponent parsedComponent) {
                editor.endEdit(parsedComponent);

                currentEditableView = null;
            }*/

            @Override
            public void cancelEdit() {
                editor.cancelEdit();

                currentEditableView = null;
            }
        });

        return editableViews[0];
    }

    public void makeEditableByMouse(JComponent valueView) {
        //valueView.setComponentPopupMenu(mouseToolSelector);

        valueView.addMouseListener(currentMouseToolWrapper);
        valueView.addMouseMotionListener(currentMouseToolWrapper);
    }

    public void unmakeEditableByMouse(JComponent valueView) {
        valueView.setComponentPopupMenu(null);

        //valueView.removeMouseListener(currentMouseToolWrapper);
        valueView.removeMouseMotionListener(currentMouseToolWrapper);
    }

    private int frameId;

    public int nextFrameId() {
        return frameId++;
    }
}
