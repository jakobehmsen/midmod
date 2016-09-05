package newlayer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public abstract class CompositeModelNode implements ModelNode {
    private ArrayList<ModelNode> children;

    public CompositeModelNode() {
        this.children = new ArrayList<>();
    }

    public CompositeModelNode(List<ModelNode> children) {
        this.children = new ArrayList<>(children);
    }

    public void add(ModelNode child) {
        children.add(child);
    }

    public void remove(ModelNode child) {
        children.remove(child);
    }

    @Override
    public JComponent buildComponent() {
        return createView(children);
    }

    protected abstract JComponent createView(List<ModelNode> children);
}
