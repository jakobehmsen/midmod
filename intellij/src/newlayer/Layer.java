package newlayer;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import sun.reflect.generics.tree.Tree;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.*;
import java.util.*;
import java.util.Timer;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Layer implements Resource {
    private Layer innerLayer;
    private String name;
    private ArrayList<ClassResource> classes = new ArrayList<>();

    public Layer(Layer innerLayer, String name) {
        this.innerLayer = innerLayer;
        this.name = name;

        if(innerLayer != null) {
            innerLayer.addObserver(new LayerObserver() {
                @Override
                public void update() {
                    try {
                        projectClasses();
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public ClassResource getClass(String name) {
        return classes.stream().filter(x -> x.getName().equals(name)).findFirst().get();
    }

    private ArrayList<LayerObserver> observers = new ArrayList<>();

    public void addObserver(LayerObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(LayerObserver observer) {
        observers.remove(observer);
    }

    private String source;

    public void setSource(String source) throws ScriptException {
        this.source = source;
        projectClasses();
    }

    private void projectClasses() throws ScriptException {
        List<ClassResource> innerLayerClasses = innerLayer != null ? innerLayer.classes : new ArrayList<>();

        classes = new ArrayList<>();
        // Should be a projected class
        innerLayerClasses.forEach(x -> addClass(x.getName()));

        if(source != null) {
            ScriptEngineManager engineManager = new ScriptEngineManager();
            NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
            engine.put("addClass", (Consumer<String>) s -> {
                addClass(s);
            });
            engine.eval(source);
        }

        observers.forEach(o -> o.update());
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

        textPane.setText(source);

        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> {
            try {
                setSource(textPane.getText());
            } catch (ScriptException se) {
                se.printStackTrace();
            }
        });

        timer.setRepeats(false);

        textPane.getStyledDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                timeUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                timeUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                timeUpdate();
            }

            private void timeUpdate() {
                timer.restart();
            }
        });

        return textPane;
    }

    public MutableTreeNode toTreeNode(DefaultTreeModel parentNode, Overview overview) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(this);

        classes.forEach(x -> {
            treeNode.add(x.toTreeNode(treeNode, overview));
        });

        addObserver(new LayerObserver() {
            @Override
            public void update() {
                Map<String, DefaultMutableTreeNode> currentClasses = ((List<DefaultMutableTreeNode>)Collections.list(treeNode.children())).stream()
                    .collect(Collectors.toMap(n -> ((ClassResource)n.getUserObject()).getName(), n -> n));

                Map<String, ClassResource> addedClasses = classes.stream().collect(Collectors.toMap(c -> c.getName(), c -> c));

                classes.forEach(c -> {
                    if(currentClasses.containsKey(c.getName())) {
                        // Update class
                        updateClass(c, (ClassResource) currentClasses.get(c.getName()).getUserObject());
                    } else {
                        // New class
                        newClass(c);
                    }
                });

                currentClasses.forEach((cName, c) -> {
                    if(!addedClasses.containsKey(cName)) {
                        // Remove class
                        removeClass((ClassResource) currentClasses.get(cName).getUserObject());
                    }
                });

                // Update order of classes

                parentNode.nodeChanged(treeNode);
            }

            private void updateClass(ClassResource newClass, ClassResource currentClass) {
                DefaultMutableTreeNode node = getView(currentClass);
                newClass.updateTreeNode(node);
            }

            private void newClass(ClassResource newClass) {
                parentNode.insertNodeInto(newClass.toTreeNode(treeNode, overview), treeNode, treeNode.getChildCount());
            }

            private void removeClass(ClassResource currentClass) {
                DefaultMutableTreeNode node = getView(currentClass);
                parentNode.removeNodeFromParent(node);
            }

            private DefaultMutableTreeNode getView(ClassResource currentClass) {
                return ((List<DefaultMutableTreeNode>)Collections.list(treeNode.children())).stream()
                    .filter(n -> ((ClassResource)n.getUserObject()).getName().equals(currentClass.getName()))
                    .findFirst().get();
            }
        });

        return treeNode;
    }

    @Override
    public String toString() {
        return name;
    }
}
