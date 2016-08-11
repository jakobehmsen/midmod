package newlayer;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Product {
    private ArrayList<Layer> layers = new ArrayList<>();

    public void addLayer(String name) {
        layers.add(new Layer(name));
    }

    public JComponent toOverview(Overview overview) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(root);

        JTree tree = new JTree(treeModel);

        tree.setRootVisible(false);

        layers.forEach(x -> {
            DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) x.toTreeNode(root, overview);
            treeModel.insertNodeInto(layerNode, root, root.getChildCount());
        });
        treeModel.nodeStructureChanged(root);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 2) {
                        DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        Resource resource = (Resource) clickedNode.getUserObject();
                        overview.open(resource);
                    }
                }
            }
        });

        return tree;
    }

    public void setSource(String source) throws ScriptException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
        engine.put("addLayer", (Consumer<String>) s -> addLayer(s));
        engine.eval(source);
    }

    public Layer getLayer(String name) {
        return layers.stream().filter(x -> x.getName().equals(name)).findFirst().get();
    }
}
