package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
        mouseToolSelector.add(createMouseToolSelector("Write", createWriteMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Move", createMoveMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Reduce", createReduceMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Delete", createDeleteMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Name", createNameMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Apply", createApplyMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Reference", createReferenceMouseTool()));

        // What if each mouse button could be a tool reference, that can be changed on the run?
        // - Then, which one should be used for mouse-over/enter/exit events?
        this.setComponentPopupMenu(mouseToolSelector);

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

            @Override
            public void mousePressed(MouseEvent e) {
                currentMouseTool.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentMouseTool.mouseReleased(e);
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
                        ValueViewContainer container = (ValueViewContainer) Stream.iterate(e.getComponent().getParent(), c -> (JComponent)c.getParent()).filter(x -> x instanceof ValueViewContainer).findFirst().get();
                        editableView = container.getEditorFor((JComponent) e.getComponent());
                    }
                    editableView.beginEdit();
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Click to create or edit an arithmetic expression - and hit enter to do the change.");
            }

            @Override
            public void endTool(JComponent component) {
                if(currentEditableView != null) {
                    currentEditableView.cancelEdit();
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

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

                    moving = true;
                    targetValueView.getParent().setComponentZOrder(targetValueView, 0);

                    int cursorType = Cursor.MOVE_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    mousePressX = e.getX();
                    mousePressY = e.getY();
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
                }
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Press and drag an object to move it.");
            }
        };
    }

    private MouseTool createReduceMouseTool() {
        return new MouseTool() {
            private JComponent selection;
            private boolean linking;
            private JComponent targetValueView;

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    linking = true;

                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

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

                    targetValueView.setForeground(Color.BLACK);

                    repaint(targetValueView.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(targetValueView, e.getPoint(), PlaygroundView.this);
                    JComponent targetComponent = (JComponent) findComponentAt(pointInContentPane);
                    Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                    if(targetComponent != targetValueView) {
                        ReductionView reduction = new ReductionView(targetValueView);

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

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    linking = true;

                    functionView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

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

                    functionView.setForeground(Color.BLACK);
                    repaint(functionView.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(valueView, e.getPoint(), PlaygroundView.this);
                    JComponent targetComponent = (JComponent) findComponentAt(pointInContentPane);
                    Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                    if(targetComponent != valueView) {
                        ApplyView applyView = new ApplyView(functionView, ((ValueView)functionView).getIdentifiers().stream().map(x -> createDefaultValueView()).collect(Collectors.toList()));

                        if(targetComponent == PlaygroundView.this) {
                            ScopeView scopeView = new ScopeView(applyView);
                            scopeView.setLocation(pointInTargetComponent);
                            add(scopeView);
                        } else {
                            JComponent targetComponentParent = (JComponent) targetComponent.getParent();
                            ((ValueViewContainer) targetComponentParent).drop(PlaygroundView.this, applyView, targetComponent);
                        }
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

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    linking = true;

                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

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

                    targetValueView.setForeground(Color.BLACK);
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

    private EditableView createRootEditableView(Supplier<String> textSupplier, Consumer<JComponent> beginEdit, Consumer<JComponent> endEdit, Runnable cancelEdit) {
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
                if(currentEditableView != null)
                    currentEditableView.cancelEdit();

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
        valueView.setComponentPopupMenu(mouseToolSelector);

        valueView.addMouseListener(currentMouseToolWrapper);
        valueView.addMouseMotionListener(currentMouseToolWrapper);
    }

    public void unmakeEditableByMouse(JComponent valueView) {
        valueView.setComponentPopupMenu(null);

        valueView.removeMouseListener(currentMouseToolWrapper);
        valueView.removeMouseMotionListener(currentMouseToolWrapper);
    }
}
