package paidia;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.Map;

public class NamedView extends JPanel implements ValueView, ValueViewContainer {
    private JComponent bodyView;
    private EditableView bodyEditableView;

    public NamedView(String name, JComponent bodyView) {
        setLayout(new BorderLayout());
        JLabel nameLabel = new JLabel(name);
        nameLabel.setOpaque(true);
        nameLabel.setBackground(Color.WHITE);
        add(nameLabel, BorderLayout.NORTH);
        this.bodyView = bodyView;

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
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
            }
        });

        add(bodyView, BorderLayout.CENTER);

        //setBorder(BorderFactory.createLineBorder(Color.RED));
        setSize(getPreferredSize());
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    @Override
    public String getText(TextContext textContext) {
        return ((ValueView)bodyView).getText(textContext);
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        //((ValueView)bodyView).setup(playgroundView);
        //playgroundView.makeEditableByMouse(() -> bodyEditableView, NamedView.this.bodyView);

        bodyEditableView = playgroundView.createEditableView(new Editor() {
            @Override
            public String getText() {
                return ((ValueView)NamedView.this.bodyView).getText(new DefaultTextContext());
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                //editorComponent.setPreferredSize(FunctionView.this.bodyView.getPreferredSize());
                editorComponent.setSize(((JTextComponent)editorComponent).getUI().getPreferredSize(editorComponent));
                //editorComponent.setSize(editorComponent.getPreferredSize());
                remove(bodyView);
                add(editorComponent, BorderLayout.CENTER);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                //((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER)
                remove(((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER));
                add(parsedComponent, BorderLayout.CENTER);
                NamedView.this.bodyView = parsedComponent;

                playgroundView.makeEditableByMouse(() -> bodyEditableView, NamedView.this.bodyView);
                ((ValueView)NamedView.this.bodyView).setup(playgroundView);

                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void cancelEdit() {
                remove(((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER));
                add(NamedView.this.bodyView, BorderLayout.CENTER);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }
        });
    }

    @Override
    public ValueView reduce(Map<String, ValueView> arguments) {
        return null;
    }

    @Override
    public void addObserver(ValueViewObserver observer) {

    }

    @Override
    public void removeObserver(ValueViewObserver observer) {

    }

    @Override
    public void release() {

    }

    @Override
    public EditableView getEditorFor(JComponent valueView) {
        return bodyEditableView;
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        return null;
    }
}
