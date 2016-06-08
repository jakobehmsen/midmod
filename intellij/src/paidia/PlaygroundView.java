package paidia;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PlaygroundView extends JPanel {
    private EditableView currentEditableView;
    private JComponent childBeingEdited;

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

                    makeEditableByMouse(() -> createRootEditableView(() -> ((ValueView) e.getChild()).getText(rootTextContext),
                        editorComponent -> {
                        remove(e.getChild());
                        childBeingEdited = (JComponent) e.getChild();
                        editorComponent.setBounds(e.getChild().getBounds());
                    }, () -> {
                        childBeingEdited = null;
                    }, () -> {
                        add(e.getChild());
                        childBeingEdited = null;
                    }), (JComponent) e.getChild());
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                revalidate();
                repaint(e.getChild().getBounds());

                e.getChild().removeComponentListener(componentAdapter);

                if(e.getChild() instanceof ValueView) {
                    ((Container)e.getChild()).removeContainerListener(this);
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                EditableView editableView = createRootEditableView(() -> "", editorComponent -> {
                    editorComponent.setLocation(e.getPoint());
                    editorComponent.setSize(80, 15);
                }, () -> { }, () -> { });

                editableView.beginEdit();
            }
        };

        addMouseListener(mouseAdapter);
    }

    private EditableView createRootEditableView(Supplier<String> textSupplier, Consumer<JComponent> beginEdit, Runnable endEdit, Runnable cancelEdit) {
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
            protected void endEdit(JComponent parsedComponent) {
                remove(editorComponent);

                parsedComponent.setLocation(editorComponent.getLocation());
                add(parsedComponent);

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
            public void endEdit(String text) {
                editor.endEdit(text);

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
                    if(valueView.getParent() != PlaygroundView.this)
                        return;

                    linking = true;
                    int cursorType = Cursor.HAND_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

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

                    //if(valueView.getParent() != PlaygroundView.this)
                    //    return;

                    moving = true;
                    targetValueView.getParent().setComponentZOrder(targetValueView, 0);
                    /*int cursorType = Cursor.MOVE_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);*/

                    mousePressX = e.getX();
                    mousePressY = e.getY();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(linking) {

                } else if(moving) {
                    if(!hasDragged) {
                        int cursorType = Cursor.MOVE_CURSOR;
                        Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                        glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                        glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);
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
                    /*linking = false;
                    int cursorType = Cursor.DEFAULT_CURSOR;
                    Component glassPane = ((RootPaneContainer)getTopLevelAncestor()).getGlassPane();
                    glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
                    glassPane.setVisible(cursorType != Cursor.DEFAULT_CURSOR);

                    remove(selection);
                    repaint(selection.getBounds());
                    revalidate();

                    Point pointInContentPane = SwingUtilities.convertPoint(valueView, e.getPoint(), PlaygroundView.this);
                    ViewBinding targetView = findView(pointInContentPane);
                    JComponent targetComponent = targetView.getView();//(JComponent) contentPane.findComponentAt(pointInContentPane);
                    Point pointInTargetComponent = SwingUtilities.convertPoint(PlaygroundView.this, pointInContentPane, targetComponent);
                    if(targetComponent != valueView) {
                        // Target must support dumping a value on it
                        Value projection = new Reduction(value.get());

                        //Value projection = value.get().createProjection();
                        ViewBinding projectionView = projection.toComponent();
                        projectionView.setupWorkspace(workspace);
                        projectionView.getView().setLocation(pointInTargetComponent);
                        targetView.drop(projection, pointInTargetComponent);
                        //targetComponent.add(projectionView.getView());

                        projection.addUsage(new Usage() {
                            ViewBinding origProjectionView = projectionView;

                            @Override
                            public void removeValue() {

                            }

                            @Override
                            public void replaceValue(Value value) {
                                if(origProjectionView.isCompatibleWith(value)) {
                                    origProjectionView.updateFrom(value);
                                    return;
                                }

                                ViewBinding projectionView = value.toComponent();
                                projectionView.setupWorkspace(workspace);
                                Point location = origProjectionView.getView().getLocation();

                                origProjectionView.drop(value, location);

                                //targetComponent.remove(origProjectionView.getView());
                                projectionView.getView().setLocation(location);
                                //targetComponent.add(projectionView.getView());
                                //targetView.drop(projection);
                                origProjectionView = projectionView;
                            }
                        });
                    }*/

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
