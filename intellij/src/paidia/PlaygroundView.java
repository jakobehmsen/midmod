package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlaygroundView extends JPanel implements ValueViewContainer {
    private EditableView currentEditableView;
    private JComponent childBeingEdited;
    private Hashtable<JComponent, EditableView> viewToEditable = new Hashtable<>();

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
        mouseToolSelector.add(createMouseToolSelector("Function", createFunctionMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Parameter", createParameterMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Name", createNameMouseTool()));
        mouseToolSelector.add(createMouseToolSelector("Apply", createApplyMouseTool()));

        // What if each mouse button could be a tool reference, that can be changed on the run?
        // - Then, which one should be used for mouse-over/enter/exit events?
        this.setComponentPopupMenu(mouseToolSelector);

        addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        e.getComponent().revalidate();
                        e.getComponent().repaint();
                    }
                };

                revalidate();
                repaint(e.getChild().getBounds());

                e.getChild().addComponentListener(componentAdapter);

                if(e.getChild() instanceof ValueView && e.getChild() != childBeingEdited) {
                    ((ValueView) e.getChild()).setup(PlaygroundView.this);
                    TextContext rootTextContext = new DefaultTextContext();
                    JComponent[] valueViewHolder = new JComponent[1];
                    valueViewHolder[0] = (JComponent) e.getChild();
                    EditableView[] editableViewHolder = new EditableView[1];
                    EditableView editableView;
                    if(childBeingEdited != null)
                        editableView = viewToEditable.get(childBeingEdited);
                    else {
                        editableView = createRootEditableView(() -> ((ValueView)valueViewHolder[0]).getText(rootTextContext),
                            editorComponent -> {
                                childBeingEdited = valueViewHolder[0];
                                remove(valueViewHolder[0]);
                                editorComponent.setBounds(valueViewHolder[0].getBounds());
                            }, newValueView -> {
                                valueViewHolder[0].removeContainerListener(this);
                                viewToEditable.remove(valueViewHolder[0]);
                                valueViewHolder[0] = newValueView;
                                viewToEditable.put(newValueView, editableViewHolder[0]);
                                childBeingEdited = null;
                            }, () -> {
                                add(valueViewHolder[0]);
                                childBeingEdited = null;
                            });
                        viewToEditable.put((JComponent) e.getChild(), editableView);
                    }
                    editableViewHolder[0] = editableView;

                    makeEditableByMouse(() -> editableView, (JComponent) e.getChild());
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                revalidate();
                repaint(e.getChild().getBounds());

                e.getChild().removeComponentListener(componentAdapter);

                if(e.getChild() instanceof ValueView && e.getChild() != childBeingEdited) {
                    ((Container)e.getChild()).removeContainerListener(this);
                    viewToEditable.remove(e.getChild());
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


                        ValueViewContainer container = (ValueViewContainer) e.getComponent().getParent();
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

            @Override
            public void mousePressed(MouseEvent e) {
                JComponent valueView = (JComponent) e.getComponent();
                if(e.getButton() == MouseEvent.BUTTON1) {
                    linking = true;

                    selection = new JPanel();
                    selection.setBorder(BorderFactory.createDashedBorder(Color.BLACK));
                    Point point = SwingUtilities.convertPoint(valueView.getParent(), valueView.getLocation(), PlaygroundView.this);
                    selection.setSize(valueView.getSize());
                    selection.setLocation(point);
                    selection.setOpaque(false);
                    add(selection, 0);
                    selection.repaint();
                    selection.revalidate();

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

                    remove(selection);
                    repaint(selection.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(valueView, e.getPoint(), PlaygroundView.this);
                    JComponent targetComponent = (JComponent) findComponentAt(pointInContentPane);
                    Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                    if(targetComponent != valueView) {
                        ReductionView reduction = new ReductionView(valueView);

                        if(valueView.getParent() == PlaygroundView.this) {
                            // Should be called "Variable" instead of EditableView?
                            EditableView editableView = viewToEditable.get(valueView);
                            // TODO: When to remove change listener?
                            editableView.addChangeListener(newValueView ->
                                reduction.setValueView((JComponent)newValueView));
                        }

                        if(targetComponent == PlaygroundView.this) {
                            reduction.setLocation(pointInTargetComponent);
                            add(reduction);
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

    private MouseTool createFunctionMouseTool() {
        return new MouseTool() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PlaygroundView.this.setToolTipText("");

                JComponent valueView = (JComponent) e.getComponent();
                //valueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();
                JComponent parent = (JComponent) valueView.getParent();
                parent.remove(valueView);
                FunctionView functionView = new FunctionView(Arrays.asList(), valueView);
                functionView.setLocation(valueView.getLocation());
                parent.add(functionView);

                revalidate();
                repaint();
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Click on an object to convert it to a function.");
            }
        };
    }

    private MouseTool createParameterMouseTool() {
        return new MouseTool() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PlaygroundView.this.setToolTipText("");

                JComponent valueView = (JComponent) e.getComponent();
                FunctionView functionView = (FunctionView) Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x instanceof FunctionView).findFirst().get();
                ValueViewContainer valueViewContainer = (ValueViewContainer) valueView.getParent();
                ChildSlot childSlot = valueViewContainer.getChildSlot(PlaygroundView.this, valueView);

                TextEditor textEditor = new TextEditor() {
                    @Override
                    protected void endEdit(String text) {
                        ParameterUsageView parameterView = functionView.makeParameterUsage(text);
                        childSlot.commit(parameterView);
                    }

                    @Override
                    protected void cancelEdit() {
                        childSlot.revert();
                    }
                };

                childSlot.replace(textEditor);

                textEditor.requestFocusInWindow();
            }

            @Override
            public void startTool(JComponent component) {
                component.setToolTipText("Click on an object to convert it to a parameter.");
            }
        };
    }

    private MouseTool createNameMouseTool() {
        return new MouseTool() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PlaygroundView.this.setToolTipText("");

                JComponent valueViewTmp = (JComponent) e.getComponent();
                JComponent valueView = Stream.iterate(valueViewTmp, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();
                ValueViewContainer valueViewContainer = (ValueViewContainer) valueView.getParent();
                ChildSlot childSlot = valueViewContainer.getChildSlot(PlaygroundView.this, valueView);

                TextEditor textEditor = new TextEditor() {
                    @Override
                    protected void endEdit(String text) {
                        childSlot.replace(new NamedView(text, valueView));
                    }

                    @Override
                    protected void cancelEdit() {
                        childSlot.revert();
                    }
                };

                textEditor.setSize(100, 15);

                childSlot.replace(textEditor);

                textEditor.requestFocusInWindow();
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
                    selection = new JPanel();
                    selection.setBorder(BorderFactory.createDashedBorder(Color.BLACK));
                    Point point = SwingUtilities.convertPoint(valueView.getParent(), valueView.getLocation(), PlaygroundView.this);
                    selection.setSize(functionView.getSize());
                    selection.setLocation(functionView.getLocation());
                    selection.setOpaque(false);
                    add(selection, 0);
                    selection.repaint();
                    selection.revalidate();

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

                    remove(selection);
                    repaint(selection.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(valueView, e.getPoint(), PlaygroundView.this);
                    JComponent targetComponent = (JComponent) findComponentAt(pointInContentPane);
                    Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                    if(targetComponent != valueView) {
                        ApplyView applyView = new ApplyView(functionView, ((ValueView)functionView).getIdentifiers().stream().map(x -> (JComponent)new AtomView("0", new BigDecimal(0))).collect(Collectors.toList()));

                        if(valueView.getParent() == PlaygroundView.this) {
                            // Should be called "Variable" instead of EditableView?
                            EditableView editableView = viewToEditable.get(valueView);
                            // TODO: When to remove change listener?
                            editableView.addChangeListener(newValueView ->
                                applyView.setValueView((JComponent)newValueView));
                        }

                        if(targetComponent == PlaygroundView.this) {
                            applyView.setLocation(pointInTargetComponent);
                            add(applyView);
                        } else {
                            JComponent targetComponentParent = (JComponent) targetComponent.getParent();
                            ((ValueViewContainer) targetComponentParent).drop(PlaygroundView.this, applyView, targetComponent);
                        }

                        //ApplicationView applicationView = functionView.makeApplication();
                        //functionView.toString();
                        /*ReductionView reduction = new ReductionView(valueView);

                        if(valueView.getParent() == PlaygroundView.this) {
                            // Should be called "Variable" instead of EditableView?
                            EditableView editableView = viewToEditable.get(valueView);
                            // TODO: When to remove change listener?
                            editableView.addChangeListener(newValueView ->
                                reduction.setValueView((JComponent)newValueView));
                        }

                        if(targetComponent == PlaygroundView.this) {
                            reduction.setLocation(pointInTargetComponent);
                            add(reduction);
                        } else {
                            JComponent targetComponentParent = (JComponent) targetComponent.getParent();
                            ((ValueView) targetComponentParent).drop(PlaygroundView.this, reduction, targetComponent);
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

    private EditableView createRootEditableView(Supplier<String> textSupplier, Consumer<JComponent> beginEdit, Consumer<JComponent> endEdit, Runnable cancelEdit) {
        return createEditableView(new ParsingEditor() {
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

                parsedComponent.setLocation(editorComponent.getLocation());

                add(parsedComponent);

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
            public void parse(JComponent editorComponent, String text, TextParseHandler handler) {
                ComponentParser.parseComponent(new ChildSlot() {
                    private JComponent currentView = editorComponent;

                    @Override
                    public void replace(JComponent view) {
                        remove(currentView);
                        view.setLocation(currentView.getLocation());
                        add(view);
                        currentView = view;
                    }

                    @Override
                    public void revert() {
                        remove(currentView);
                    }

                    @Override
                    public void commit(JComponent valueView) {
                        remove(currentView);
                        valueView.setLocation(currentView.getLocation());
                        add(valueView);
                    }
                }, text, handler);
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
            public void endEdit(JComponent parsedComponent) {
                editor.endEdit(parsedComponent);

                currentEditableView = null;
            }

            @Override
            public void cancelEdit() {
                editor.cancelEdit();

                currentEditableView = null;
            }
        });

        return editableViews[0];
    }

    public void makeEditableByMouse(Supplier<EditableView> editableViewSupplier, JComponent valueView) {
        valueView.setComponentPopupMenu(mouseToolSelector);

        valueView.addMouseListener(currentMouseToolWrapper);
        valueView.addMouseMotionListener(currentMouseToolWrapper);
    }

    @Override
    public EditableView getEditorFor(JComponent valueView) {
        return viewToEditable.get(valueView);
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        return new ChildSlot() {
            JComponent currentView = valueView;

            @Override
            public void replace(JComponent view) {
                remove(currentView);
                view.setLocation(currentView.getLocation());
                add(view);
                currentView = view;
            }

            @Override
            public void revert() {
                remove(currentView);
                add(valueView);
            }

            @Override
            public void commit(JComponent valueView) {
                remove(currentView);
                valueView.setLocation(currentView.getLocation());
                add(valueView);
            }
        };
    }
}
