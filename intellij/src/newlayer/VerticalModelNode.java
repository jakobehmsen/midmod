package newlayer;

import javax.swing.*;
import java.util.List;

public class VerticalModelNode extends CompositeModelNode {
    public VerticalModelNode() { }

    public VerticalModelNode(List<ModelNode> children) {
        super(children);
    }

    private boolean addGlue;

    public void dontAddGlue() {
        addGlue = false;
    }

    @Override
    protected JComponent createView(List<ModelNode> children) {
        Box view = Box.createVerticalBox();

        children.forEach(x -> view.add(x.buildComponent()));
        if(addGlue)
            view.add(Box.createVerticalGlue());

        return view;
    }
}
