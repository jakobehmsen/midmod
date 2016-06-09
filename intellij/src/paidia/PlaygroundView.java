package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PlaygroundView extends JPanel {
    private EditableView currentEditableView;
    private JComponent childBeingEdited;
    private Hashtable<JComponent, EditableView> viewToEditable = new Hashtable<>();

    public PlaygroundView() {
        setLayout(null);

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
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                EditableView editableView = createRootEditableView(() -> "", editorComponent -> {
                    editorComponent.setLocation(e.getPoint());
                    editorComponent.setSize(80, 15);
                }, newValueView -> { }, () -> { });

                editableView.beginEdit();
            }
        };

        addMouseListener(mouseAdapter);
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

        editableViews[0] = new EditableView(new Editor() {
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
        valueView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    EditableView editableView = editableViewSupplier.get();
                    editableView.beginEdit();
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private JComponent selection;
            private JComponent targetValueView;
            private int mousePressX;
            private int mousePressY;
            private boolean linking;
            private boolean moving;
            private boolean hasDragged;

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {
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
                } else if(e.getButton() == MouseEvent.BUTTON1) {
                    targetValueView = Stream.iterate(valueView, c -> (JComponent)c.getParent()).filter(x -> x.getParent() == PlaygroundView.this).findFirst().get();

                    moving = true;
                    targetValueView.getParent().setComponentZOrder(targetValueView, 0);

                    mousePressX = e.getX();
                    mousePressY = e.getY();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(linking) {
                    if(!hasDragged) {
                        int cursorType = Cursor.HAND_CURSOR;
                        Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                        glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                        glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                        hasDragged = true;
                    }
                } else if(moving) {
                    if(!hasDragged) {
                        int cursorType = Cursor.MOVE_CURSOR;
                        Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                        glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                        glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                        hasDragged = true;
                    }

                    int deltaX = e.getX() - mousePressX;
                    int deltaY = e.getY() - mousePressY;

                    targetValueView.setLocation(targetValueView.getX() + deltaX, targetValueView.getY() + deltaY);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                hasDragged = false;

                if(e.getButton() == MouseEvent.BUTTON3 && linking) {
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
                        ReductionView projection = new ReductionView(valueView);

                        if(valueView.getParent() == PlaygroundView.this) {
                            // Should be called "Variable" instead of EditableView?
                            EditableView editableView = viewToEditable.get(valueView);
                            // TODO: When to remove change listener?
                            editableView.addChangeListener(newValueView ->
                                 projection.setValueView((JComponent)newValueView));
                        }

                        if(targetComponent == PlaygroundView.this) {
                            projection.setLocation(pointInTargetComponent);
                            add(projection);
                        } else {
                            JComponent targetComponentParent = (JComponent) targetComponent.getParent();
                            ((ValueView) targetComponentParent).drop(PlaygroundView.this, projection, targetComponent);
                        }
                    }

                } else if(e.getButton() == MouseEvent.BUTTON1 && moving) {
                    moving = false;
                    int cursorType = Cursor.DEFAULT_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
                }
            }
        };

        valueView.addMouseListener(mouseAdapter);
        valueView.addMouseMotionListener(mouseAdapter);
    }
}
