package newlayer;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Layer implements Resource {
    private String name;
    private ArrayList<ClassResource> classes = new ArrayList<>();

    public Layer(String name) {
        this.name = name;
    }

    public void setSource(String source) throws ScriptException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
        engine.put("addClass", (Consumer<String>) s -> addClass(s));
        engine.eval(source);
    }

    public void addClass(String name) {
        classes.add(new ClassResource(name));
    }

    public JComponent toDesign() {
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public JComponent toView() {
        JTextPane textPane = new JTextPane();

        return textPane;
    }

    public Layer project(Layer previousLayer) {
        ArrayList<ClassResource> allClasses = new ArrayList<>();

        allClasses.addAll(previousLayer.classes);
        allClasses.addAll(classes);

        Layer projectedLayer = new Layer(name);

        projectedLayer.classes = allClasses;

        return projectedLayer;
    }

    public MutableTreeNode toTreeNode(DefaultMutableTreeNode parentNode, Overview overview) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(this);

        classes.forEach(x -> {

            treeNode.add(x.toTreeNode(treeNode, overview));
        });

        return treeNode;
    }

    @Override
    public String toString() {
        return name;
    }
}
