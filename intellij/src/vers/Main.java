package vers;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Main {
    public static class NodeWrapper {
        public JTree tree;

        public DefaultMutableTreeNode node;

        public NodeWrapper() {
            tree = new JTree();
            tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Root")));
            node = (DefaultMutableTreeNode)(tree.getModel()).getRoot();
            tree.setRootVisible(false);
        }

        public NodeWrapper(JTree tree, DefaultMutableTreeNode node) {
            this.tree = tree;
            this.node = node;
        }

        public NodeWrapper get(String name) {
            return new NodeWrapper(tree, getNode(name));
        }

        public NodeWrapper getRoot() {
            return new NodeWrapper(tree, (DefaultMutableTreeNode)tree.getModel().getRoot());
        }

        private DefaultMutableTreeNode getNode(String name) {
            return (DefaultMutableTreeNode)Collections.list(node.children()).stream().filter(x -> ((DefaultMutableTreeNode)x).getUserObject().equals(name)).findFirst().get();
        }

        public NodeWrapper prepend(String name) {
            return insert(name, 0);
        }

        public NodeWrapper append(String name) {
            return insert(name, node.getChildCount());
        }

        public NodeWrapper remove(String name) {
            DefaultMutableTreeNode nodeWithName = getNode(name);
            ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(nodeWithName);
            ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
            return new NodeWrapper(tree, nodeWithName);
        }

        public void moveTo(NodeWrapper target, int index) {
            ((DefaultTreeModel)tree.getModel()).removeNodeFromParent(node);
            target.insert((String)node.getUserObject(), index);
            ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
        }

        public NodeWrapper insert(String name, int index) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(name);
            ((DefaultTreeModel)tree.getModel()).insertNodeInto(newNode, node, index);
            ((DefaultTreeModel)tree.getModel()).nodeChanged(node);
            TreePath pathToNodeChanged = new TreePath(((DefaultTreeModel)tree.getModel()).getPathToRoot(node));
            if(!tree.isExpanded(pathToNodeChanged))
                tree.expandPath(pathToNodeChanged);
            return new NodeWrapper(tree, newNode);
        }
    }

    public static void main(String[] args) {
        NodeWrapper root = new NodeWrapper();

        ScriptEngineManager engineManager = new ScriptEngineManager();
        //NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");

        Supplier<NashornScriptEngine> engineSupplier = new Supplier<NashornScriptEngine>() {
            NashornScriptEngine engine;

            @Override
            public NashornScriptEngine get() {
                if(engine == null) {
                    engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
                    engine.put("root", root);

                    Arrays.asList(root.getClass().getDeclaredMethods()).stream().forEach(x -> {
                        String params = IntStream.range(0, x.getParameterCount()).mapToObj(i -> "arg" + i).collect(Collectors.joining(", "));
                        try {
                            String script = x.getName() + " = function(" + params + ") { return root." + x.getName() + "(" + params + ") }";
                            engine.eval(script);
                        } catch (ScriptException e) {
                            e.printStackTrace();
                        }
                    });
                }

                return engine;
            }
        };

        //engine.put("root", root);

        /*Arrays.asList(root.getClass().getDeclaredMethods()).stream().forEach(x -> {
            String params = IntStream.range(0, x.getParameterCount()).mapToObj(i -> "arg" + i).collect(Collectors.joining(", "));
            try {
                String script = x.getName() + " = function(" + params + ") { return root." + x.getName() + "(" + params + ") }";
                engine.eval(script);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        });*/

        JFrame frame = new JFrame("Vers - Tree");

        JTextPane scripterText = new JTextPane();
        scripterText.setBackground(Color.BLACK);
        scripterText.setForeground(Color.WHITE);
        scripterText.setCaretColor(scripterText.getForeground());
        scripterText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        scripterText.registerKeyboardAction(e -> {
            String text = scripterText.getText();

                try {
                    engineSupplier.get().eval(text);

                    try {
                        DataOutputStream outputStream = new DataOutputStream(Files.newOutputStream(Paths.get("journal.jnl"), StandardOpenOption.CREATE, StandardOpenOption.APPEND));
                        outputStream.writeUTF(text);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } catch (ScriptException e1) {
                    try {
                        scripterText.getDocument().insertString(scripterText.getDocument().getLength(), "\n// Error: " + e1.getMessage(), null);
                    } catch (BadLocationException e2) {
                        e2.printStackTrace();
                    }
                }
            },
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK), JComponent.WHEN_FOCUSED);
        JComponent scripter = scripterText;

        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        splitter.setTopComponent(new JScrollPane(root.tree));
        splitter.setBottomComponent(scripter);
        splitter.setDividerLocation(500);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(splitter, BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                new Thread(() -> {
                    String title = frame.getTitle();
                    frame.setTitle(title + " - Loading...");
                    frame.setEnabled(false);

                    try {
                        DataInputStream inputStream = new DataInputStream(newInputStream(Paths.get("journal.jnl")));
                        while(inputStream.available() > 0) {
                            String text = inputStream.readUTF();
                            try {
                                engineSupplier.get().eval(text);
                            } catch (ScriptException e2) {
                                e2.printStackTrace();
                            }
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                    frame.setEnabled(true);
                    frame.setTitle(title);
                }).run();
            }
        });

        frame.setVisible(true);
    }
}
