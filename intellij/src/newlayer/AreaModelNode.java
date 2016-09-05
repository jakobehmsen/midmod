package newlayer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AreaModelNode extends HorizontalModelNode {
    public AreaModelNode() { }

    public AreaModelNode(List<ModelNode> children) {
        super(children);
    }

    @Override
    protected void preChildren(Box view) {
        view.add(Box.createRigidArea(new Dimension(20, 0)));
    }
}
