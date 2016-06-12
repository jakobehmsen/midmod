package paidia;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class FunctionView extends JPanel implements ValueView, ValueViewContainer {
    private List<String> parameters;
    private JComponent bodyView;
    private EditableView bodyEditableView;
    //private ComponentListener componentListener;

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

        /*componentListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setSize(getPreferredSize());
            }
        };*/

        //bodyView.addComponentListener(componentListener);
        add(bodyView);

        setBorder(BorderFactory.createLineBorder(Color.RED));
        setSize(getPreferredSize());
    }

    @Override
    public String getText(TextContext textContext) {
        return "function() {\n    " + ((ValueView)bodyView).getText(textContext) + "\n}";
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public void setup(PlaygroundView playgroundView) {
        //((ValueView)bodyView).setup(playgroundView);

        bodyEditableView = playgroundView.createEditableView(new Editor() {
            @Override
            public String getText() {
                return ((ValueView)FunctionView.this.bodyView).getText(new DefaultTextContext());
            }

            @Override
            public void beginEdit(JComponent editorComponent) {
                //editorComponent.setPreferredSize(FunctionView.this.bodyView.getPreferredSize());
                editorComponent.setSize(((JTextComponent)editorComponent).getUI().getPreferredSize(editorComponent));
                //editorComponent.setSize(editorComponent.getPreferredSize());
                remove(0);
                //bodyView.removeComponentListener(componentListener);
                add(editorComponent, 0);
                //bodyView.addComponentListener(componentListener);
                setSize(getPreferredSize());

                repaint();
                revalidate();
            }

            @Override
            public void endEdit(JComponent parsedComponent) {
                remove(0);
                add(parsedComponent, 0);
                FunctionView.this.bodyView = parsedComponent;

                playgroundView.makeEditableByMouse(() -> bodyEditableView, FunctionView.this.bodyView);
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
    public ValueView reduce() {
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
}
