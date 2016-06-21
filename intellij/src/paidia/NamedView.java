package paidia;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.ArrayList;
import java.util.Map;

public class NamedView extends JPanel implements ValueView, ValueViewContainer {
    private JComponent bodyView;
    private EditableView bodyEditableView;
    private ValueViewObserver observer;

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

        observer = new ValueViewObserver() {
            @Override
            public void updated() {
                update();
            }
        };
        ((ValueView)bodyView).addObserver(observer);
    }

    @Override
    public String getSource(TextContext textContext) {
        return ((ValueView)bodyView).getSource(textContext);
    }

    @Override
    public void setText(String text) {

    }

    private void update() {
        observers.forEach(x -> x.updated());

        setSize(getPreferredSize());

        repaint();
        revalidate();
    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        //((ValueView)bodyView).setup(playgroundView);
        //playgroundView.makeEditableByMouse(() -> bodyEditableView, NamedView.this.bodyView);

        bodyEditableView = playgroundView.createEditableView(new ParsingEditor(playgroundView) {
            @Override
            public String getText() {
                return ((ValueView)NamedView.this.bodyView).getSource(new DefaultTextContext());
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

                playgroundView.makeEditableByMouse(NamedView.this.bodyView);
                ((ValueView)NamedView.this.bodyView).setup(playgroundView);

                update();
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
    public ValueView evaluate(Map<String, ValueView> environment) {
        return ((ValueView)bodyView).evaluate(environment);
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
