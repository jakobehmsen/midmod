package newlayer;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Layer implements Resource {
    private LayerPersistor layerPersistor;
    private String name;
    private ArrayList<ClassResource> classes = new ArrayList<>();

    public Layer(LayerPersistor layerPersistor, String name) {
        this.layerPersistor = layerPersistor;
        this.name = name;
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
        observers.forEach(o -> o.requestUpdate(this));
    }

    private void projectClasses(Layer innerLayer) throws ScriptException {
        List<ClassResource> innerLayerClasses = innerLayer != null ? innerLayer.classes : new ArrayList<>();

        List<ClassResource> oldClasses = classes;

        classes = new ArrayList<>();

        innerLayerClasses.forEach(x -> {
            classes.add(x.copy(this));
        });

        if(source != null) {
            ScriptEngineManager engineManager = new ScriptEngineManager();
            NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
            engine.put("addClass", (Consumer<String>) s -> {
                addClass(s);
            });
            engine.put("getClass", (Function<String, ClassResource>) s -> {
                return getClass(s);
            });
            engine.eval(source);
        }

        for(int i = 0; i < classes.size(); i++) {
            ClassResource newClass = classes.get(i);
            int index = i;
            oldClasses.stream().filter(x -> x.getName().equals(newClass.getName())).findFirst().ifPresent(x ->
            {
                x.updateFrom(newClass);
                classes.set(index, x);
            });
        }

        observers.forEach(o -> o.wasUpdated(this));
    }

    public void addClass(String name) {
        classes.add(new ClassResource(this, name));
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

    @Override
    public Resource getParent() {
        return null;
    }

    public MutableTreeNode toTreeNode(DefaultTreeModel parentNode, Overview overview) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(this);

        classes.forEach(x -> {
            treeNode.add(x.toTreeNode(treeNode, overview));
        });

        addObserver(new LayerObserver() {
            @Override
            public void wasUpdated(Layer layer) {
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

            @Override
            public void requestUpdate(Layer layer) {

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

    public void updateFrom(Layer layer) {
        try {
            projectClasses(layer);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        layerPersistor.save(this);
    }

    public String getSource() {
        return source;
    }
}
