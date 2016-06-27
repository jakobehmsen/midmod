package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

public class Value2ViewWrapper extends JPanel {
    private Value2Holder value2Holder;
    private JComponent view;

    public Value2ViewWrapper(Value2Holder value2Holder, JComponent view) {
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

    public void beginEdit(PlaygroundView playgroundView) {
        playgroundView.createEditableView(new ParsingEditor(playgroundView) {
            JComponent editorComponent;

            @Override
            public String getText() {
                return getValue().getText();
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                this.editorComponent = editorComponent;

                editorComponent.setSize(view.getPreferredSize());

                remove(view);
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

                repaint();
                revalidate();
            }

            @Override
            protected void endEdit(Value2 parsedValue) {
                remove(editorComponent);

                ViewBinding2 viewBinding = parsedValue.toView(playgroundView);

                JComponent scopeView = viewBinding.getComponent();

                scopeView.setLocation(editorComponent.getLocation());

                value2Holder.setValue(parsedValue);

                add(scopeView);
                view = scopeView;

                repaint();
                revalidate();
            }

            @Override
            public void cancelEdit() {
                remove(editorComponent);
                add(view);

                repaint();
                revalidate();
            }
        }).beginEdit();
    }

    public Value2 getValueHolder() {
        return value2Holder;
    }
}
