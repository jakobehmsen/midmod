package newlayer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class ClassResource implements Resource {
    private String name;

    public ClassResource(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JComponent toView() {
        return null;
    }

    public JComponent toDesign() {
        return null;
    }

    public DefaultMutableTreeNode toTreeNode(DefaultMutableTreeNode parentNode, Overview overview) {
        return new DefaultMutableTreeNode(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
