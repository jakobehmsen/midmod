package paidia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BlockView extends CompositeValueView {
    public BlockView(List<ValueView> expressions) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        expressions.forEach(x -> {
            addChild(x);
        });
    }

    @Override
    public String getText(TextContext textContext) {
        return getChildren().stream().map(x -> x.getText(textContext)).collect(Collectors.joining("\n"));
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public ValueView evaluate(Map<String, ValueView> environment) {
        for(int i = 0; i < getChildren().size() - 1; i++)
            getChildren().get(i).evaluate(environment);

        return getChildren().get(getChildren().size() - 1);
    }

    @Override
    protected void addChildAsComponent(int index, JComponent child) {
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

        b.add(child);
        b.add(Box.createHorizontalGlue());
        super.addChildAsComponent(index, b);


        /*if(index == 0)
            add(Box.createHorizontalGlue(), index * 2);
        else
            add(Box.createRigidArea(new Dimension(10, 0)), index * 2);
        super.addChildAsComponent((index * 2) + 1, child);*/
    }

    @Override
    protected int zOrderFromChild(JComponent child) {
        return IntStream.range(0, getComponentCount()).filter(x ->
            ((Box)getComponent(x)).getComponent(0) == child).findFirst().getAsInt();
    }

    /*@Override
    protected void removeChildAsComponent(int index, JComponent child) {
        remove(index * 2);
        remove(index * 2);
    }*/
}
