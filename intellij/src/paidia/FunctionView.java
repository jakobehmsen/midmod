package paidia;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionView extends JPanel implements ValueView, ValueViewContainer {
    private List<String> parameters;
    private JComponent bodyView;
    private EditableView bodyEditableView;

    public FunctionView(List<String> parameters, JComponent bodyView) {
        this.parameters = new ArrayList<>(parameters);
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

        add(bodyView);

        setBorder(BorderFactory.createLineBorder(Color.RED));
        setSize(getPreferredSize());
    }

    @Override
    public String getSource(TextContext textContext) {
        return "function() {\n    " + ((ValueView)bodyView).getSource(textContext) + "\n}";
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        ((ValueView)bodyView).setup(playgroundView);
        playgroundView.makeEditableByMouse(this.bodyView);

        bodyEditableView = playgroundView.createEditableView(new Editor() {
            @Override
            public String getText() {
                return ((ValueView)FunctionView.this.bodyView).getSource(new DefaultTextContext());
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                //editorComponent.setPreferredSize(FunctionView.this.bodyView.getPreferredSize());
                editorComponent.setSize(((JTextComponent)editorComponent).getUI().getPreferredSize(editorComponent));
                //editorComponent.setSize(editorComponent.getPreferredSize());
                remove(0);
                add(editorComponent, 0);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                remove(0);
                add(parsedComponent, 0);
                FunctionView.this.bodyView = parsedComponent;

                playgroundView.makeEditableByMouse(FunctionView.this.bodyView);
                ((ValueView)FunctionView.this.bodyView).setup(playgroundView);

                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void cancelEdit() {
                remove(0);
                add(FunctionView.this.bodyView);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }
        });
    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        return this;
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

    public ParameterUsageView makeParameterUsage(String name) {
        ParameterUsageView parameterUsageView = new ParameterUsageView(name);

        if(!parameters.contains(name))
            parameters.add(name);

        return parameterUsageView;
    }

    @Override
    public EditableView getEditorFor(JComponent valueView) {
        return bodyEditableView;
    }

    @Override
    public ChildSlot getChildSlot(PlaygroundView playgroundView, JComponent valueView) {
        return null;
    }

    public ValueView apply(List<JComponent> arguments) {
        return null;
    }

    public List<String> getParameterNames() {
        return parameters;
    }
}
