package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BinaryValue2 extends AbstractValue2 implements Value2Observer {
    private Text operator;
    private TextContext textOperator;
    private Value2 lhs;
    private Value2 rhs;
    private Function<Value2[], Value2> reducer;

    public BinaryValue2(Text text, TextContext textOperator, Value2 lhs, Value2 rhs, Function<Value2[], Value2> reducer) {
        this.operator = text;
        this.textOperator = textOperator;
        this.lhs = lhs;
        this.rhs = rhs;
        this.reducer = reducer;

        lhs.addObserver(this);
        rhs.addObserver(this);
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JPanel view = new JPanel();

        view.setAlignmentX(Component.LEFT_ALIGNMENT);

        ((FlowLayout)view.getLayout()).setHgap(0);
        ((FlowLayout)view.getLayout()).setVgap(0);

        ComponentUtil.addObserverCleanupLogic(this, view, () -> {
            view.setSize(view.getPreferredSize());
            view.revalidate();
            view.repaint();
        });

        view.addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        view.setSize(view.getPreferredSize());
                    }
                };

                e.getChild().addComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
                view.setSize(view.getPreferredSize());
            }
        });

        view.add(lhs.toView(playgroundView).getComponent());
        view.add(new JLabel(" " + operator.getRaw() + " "));
        view.add(rhs.toView(playgroundView).getComponent());

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return view;
            }
        };
    }

    @Override
    public String getText() {
        return getSource(new DefaultTextContext());
    }

    @Override
    public String getSource(TextContext textContext) {
        String text = lhs.getSource(textOperator) + operator.getRaw() + rhs.getSource(textOperator);

        return textOperator.getText(textContext, text);
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        return reducer.apply(new Value2[]{lhs.reduce(environment), rhs.reduce(environment)});
    }

    @Override
    public void updated() {
        sendUpdated();
    }

    @Override
    public void appendParameters(List<String> parameters) {
        lhs.appendParameters(parameters);
        rhs.appendParameters(parameters);
    }
}
