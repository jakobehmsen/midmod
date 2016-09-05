package newlayer;

import javax.swing.*;
import java.util.List;

public class HorizontalModelNode extends CompositeModelNode {
    public HorizontalModelNode() { }

    public HorizontalModelNode(List<ModelNode> children) {
        super(children);
    }

    @Override
    protected JComponent createView(List<ModelNode> children) {
        Box view = Box.createHorizontalBox();

        preChildren(view);
        children.forEach(x -> view.add(x.buildComponent()));
        postChildren(view);

        return view;
    }

    protected void preChildren(Box view) {

    }

    protected void postChildren(Box view) {
        view.add(Box.createHorizontalGlue());
    }
}
