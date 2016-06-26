package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.*;
import java.util.List;

public class IfValue2 extends AbstractValue2 implements Value2Observer {
    private Value2 condition;
    private Value2 trueExpression;
    private Value2 falseExpression;

    public IfValue2(Value2 condition, Value2 trueExpression, Value2 falseExpression) {
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;

        condition.addObserver(this);
        trueExpression.addObserver(this);
        falseExpression.addObserver(this);
    }

    @Override
    public ViewBinding2 toView(PlaygroundView playgroundView) {
        JPanel view = new JPanel();

        view.setAlignmentX(Component.LEFT_ALIGNMENT);
        view.setAlignmentY(Component.TOP_ALIGNMENT);

        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));

        view.add(createLine("if", condition.toView(playgroundView).getComponent()));
        view.add(createLine("then", trueExpression.toView(playgroundView).getComponent()));
        view.add(createLine("else", falseExpression.toView(playgroundView).getComponent()));

        return new ViewBinding2() {
            @Override
            public JComponent getComponent() {
                return view;
            }
        };
    }

    private JComponent createKeywordComponent(String text) {
        JLabel keywordComponent = new JLabel(text);

        keywordComponent.setForeground(Color.BLUE);

        return keywordComponent;
    }

    private JComponent createLine(String keyword, JComponent child) {
        Box b = Box.createHorizontalBox();

        b.addContainerListener(new ContainerAdapter() {
            ComponentAdapter componentAdapter;

            @Override
            public void componentAdded(ContainerEvent e) {
                componentAdapter = new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        b.setSize(b.getPreferredSize());
                    }
                };

                e.getChild().addComponentListener(componentAdapter);
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getChild().removeComponentListener(componentAdapter);
            }
        });

        JComponent keywordComponent = createKeywordComponent(keyword);

        b.add(keywordComponent);
        b.add(Box.createHorizontalStrut(5));
        b.add(child);
        b.add(Box.createHorizontalGlue());

        return b;
    }

    @Override
    public String getText() {
        return getSource(new DefaultTextContext());
    }

    @Override
    public String getSource(TextContext textContext) {
        return "if " + condition.getSource(textContext) +
            " then " + trueExpression.getSource(textContext) +
            " else " + falseExpression.getSource(textContext);
    }

    @Override
    public Value2 reduce(Map<String, Value2> environment) {
        Value2 result = condition.reduce(environment);
        if((boolean)((AtomValue2)result).getValue())
            return trueExpression.reduce(environment);
        return falseExpression.reduce(environment);
    }

    @Override
    public void updated() {
        sendUpdated();
    }

    @Override
    public void appendParameters(List<String> parameters) {
        condition.appendParameters(parameters);
        trueExpression.appendParameters(parameters);
        falseExpression.appendParameters(parameters);
    }
}
