package paidia;

import javax.swing.*;
import java.awt.event.*;

public class PlaygroundView extends JPanel {
    private EditableView currentEditableView;

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
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                revalidate();
                repaint(e.getChild().getBounds());

                e.getChild().removeComponentListener(componentAdapter);
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                EditableView editableView = createEditableView(new Editor() {
                    JComponent editorComponent;

                    @Override
                    public String getText() {
                        return "";
                    }

                    @Override
                    public void beginEdit(JComponent editorComponent) {
                        this.editorComponent = editorComponent;

                        editorComponent.setLocation(e.getPoint());
                        editorComponent.setSize(200, 30);

                        add(editorComponent);

                        repaint();
                        revalidate();
                    }

                    @Override
                    public void endEdit(String text) {
                        remove(editorComponent);

                        repaint();
                        revalidate();
                    }

                    @Override
                    public void cancelEdit() {
                        remove(editorComponent);

                        repaint();
                        revalidate();
                    }
                });

                editableView.beginEdit();
            }
        };

        addMouseListener(mouseAdapter);
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

    public void makeEditableByMouse(EditableView editableView, JComponent valueView) {
        valueView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    editableView.beginEdit();
                }
            }
        });
    }
}
