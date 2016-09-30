package newlayer;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.NodeOperatorVisitor;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.function.Consumer;

public class Main {
    private static Product product;
    private static boolean isSaved;
    private static boolean isLoading;
    private static String folderToSaveIn;

    public static void main(String[] args) throws ScriptException {
        String src = "m(0, x())";
        String src2 = Layer.modifiedSource("Name", src);

        System.out.println(src);
        System.out.println("=>");
        System.out.println(src2);

        // From http://stackoverflow.com/questions/6511556/javascript-parser-for-java
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager();
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());
        Source source   = Source.sourceFor("test",
            "var a = function() { return 1; }\n" +
            "var a = 0\n" +
            "var b = a + 1\n" +
            "function someFunction() { return b + 1; }  ");

        Parser parser = new Parser(context.getEnv(), source, errors);
        FunctionNode program = parser.parse();

        StringBuilder sb = new StringBuilder();

        program.getBody().getStatements().forEach(s -> {
            s.accept(new NodeOperatorVisitor<LexicalContext>(new LexicalContext()) {
                private int depth;

                @Override
                protected boolean enterDefault(Node node) {
                    if(depth == 0)
                        sb.append("enter(" + node.getStart() + ", " + node.getFinish() + ");\n");

                    depth++;

                    return super.enterDefault(node);
                }

                @Override
                protected Node leaveDefault(Node node) {
                    depth--;

                    if(depth == 0)
                        sb.append("leave();\n");

                    return super.leaveDefault(node);
                }

                @Override
                public boolean enterVarNode(VarNode varNode) {
                    super.enterVarNode(varNode);

                    sb.append("var " + varNode.getName().getName() + " = ");

                    return true;
                }

                @Override
                public Node leaveVarNode(VarNode varNode) {

                    sb.append("\n");

                    return super.leaveVarNode(varNode);
                }

                @Override
                public boolean enterLiteralNode(LiteralNode<?> literalNode) {
                    if(literalNode.getType().isInteger())
                        sb.append(literalNode.getValue());

                    return super.enterLiteralNode(literalNode);
                }

                @Override
                public Node leaveLiteralNode(LiteralNode<?> literalNode) {
                    return super.leaveLiteralNode(literalNode);
                }

                @Override
                public boolean enterFunctionNode(FunctionNode functionNode) {
                    return super.enterFunctionNode(functionNode);
                }
            });
            System.out.println(source.getString(s.getStart(), s.getFinish() - s.getStart()));
        });

        LayerFactory layerFactory = new LayerFactory() {
            @Override
            public Layer createLayer(String name) {
                return createLayer(name, true);
            }

            private Layer createLayer(String name, boolean isNew) {
                return new Layer(new LayerPersistor() {
                    private boolean hasChanges = isNew;
                    private String lastName = name;
                    private String currentName = name;

                    @Override
                    public void save(Layer layer) {
                        if(!hasChanges)
                            return;

                        if(!currentName.equals(lastName)) {
                            String lastLayerFile = getLayerFile(lastName);
                            try {
                                java.nio.file.Files.delete(Paths.get(lastLayerFile));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            lastName = currentName;
                        }

                        String layerFile = getLayerFile(currentName);

                        try {
                            java.nio.file.Files.write(Paths.get(layerFile), layer.getSource().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void changeName(Layer layer, String newName) {
                        currentName = newName;

                        if(isLoading)
                            return;

                        hasChanges = true;
                    }

                    @Override
                    public void changedSource(Layer layer) {
                        if(isLoading)
                            return;

                        hasChanges = true;
                    }
                }, name);
            }

            private String getLayerFile(String name) {
                return Paths.get(folderToSaveIn, "layers", name + ".layer").toString();
            }

            @Override
            public Layer openLayer(String name) {
                Layer layer = createLayer(name, false);

                String layerFile = getLayerFile(name);
                try {
                    String source = new String(java.nio.file.Files.readAllBytes(Paths.get(layerFile)));
                    layer.setSource(source);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ScriptException e) {
                    e.printStackTrace();
                }

                return layer;
            }

            @Override
            public void allocateForPersistence() {
                String layerFolder = Paths.get(folderToSaveIn, "layers").toString();
                if(!new File(layerFolder).exists())
                    new File(layerFolder).mkdir();
            }
        };

        ProductPersistor productPersistor = new ProductPersistor() {
            private boolean hasChanges;

            @Override
            public void saveProduct(Product product) {
                if(!hasChanges)
                    return;

                try {
                    String fileToSaveIn = Paths.get(folderToSaveIn, "product").toString();
                    OutputStream output = new FileOutputStream(fileToSaveIn);
                    product.writeTo(output);
                    output.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                hasChanges = false;
            }

            @Override
            public void allocateForPersistence() {
                if(!new File(folderToSaveIn).exists())
                    new File(folderToSaveIn).mkdir();

                layerFactory.allocateForPersistence();
            }

            @Override
            public void layerNameChanged(Layer layer) {
                if(isLoading)
                    return;

                hasChanges = true;
            }

            @Override
            public void addedLayer(Product product, Layer layer, int index) {
                if(isLoading)
                    return;

                hasChanges = true;
            }

            @Override
            public void removedLayer(Product product, Layer layer, int index) {
                if(isLoading)
                    return;

                hasChanges = true;
            }
        };

        JFrame frame = new JFrame("NewLayer");

        JPanel contentPane = (JPanel) frame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        MouseAdapter mouseAdapter = new MouseAdapter() {
            private boolean dragging;
            private int tabIndex;
            private String tabTitle;
            private JComponent tabComponent;

            @Override
            public void mousePressed(MouseEvent e) {
                int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                if(tabIndex != -1) {
                    dragging = true;
                    this.tabIndex = tabIndex;
                    tabComponent = (JComponent) tabbedPane.getComponent(tabIndex);
                    tabTitle = tabbedPane.getTitleAt(tabIndex);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(dragging) {
                    tabTitle = null;
                    tabComponent = null;
                    tabIndex = -1;
                    dragging = false;
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(dragging) {
                    int tabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if(tabIndex != -1 && this.tabIndex != tabIndex) {
                        if(tabIndex < this.tabIndex) {
                            tabbedPane.removeTabAt(this.tabIndex);
                            tabbedPane.insertTab(tabTitle, null, tabComponent, null, tabIndex);
                            this.tabIndex = tabIndex;
                        } else {
                            tabbedPane.removeTabAt(this.tabIndex);
                            tabbedPane.insertTab(tabTitle, null, tabComponent, null, tabIndex);
                            this.tabIndex = tabIndex;
                        }

                        tabbedPane.setSelectedIndex(this.tabIndex);
                    }
                }
            }
        };

        tabbedPane.addMouseListener(mouseAdapter);
        tabbedPane.addMouseMotionListener(mouseAdapter);

        Overview overview = new Overview() {
            private Hashtable<Resource, ViewBinding<JComponent>> openedResources = new Hashtable<>();

            @Override
            public void open(Resource resource) {
                if(openedResources.containsKey(resource)) {
                    ViewBinding<JComponent> resourceView = openedResources.get(resource);
                    int tabIndex = tabbedPane.indexOfComponent(resourceView.getView());
                    tabbedPane.setSelectedIndex(tabIndex);
                } else {
                    ViewBinding<JComponent> resourceView = resource.toView(this);
                    tabbedPane.add(resource.getPath(), resourceView.getView());
                    openedResources.put(resource, resourceView);
                    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                }
            }

            @Override
            public ViewBinding<JComponent> getView(Resource resource) {
                return openedResources.get(resource);
            }
        };

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        JTree tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setToggleClickCount(0);

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, null/*product.toOverview(tree, treeModel, root, overview)*/, tabbedPane);

        splitPane.setDividerLocation(200);

        contentPane.add(splitPane, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();

        toolBar.setFloatable(false);

        AbstractAction saveProductAction = new AbstractAction("Save product...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();

                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if(!isSaved) {
                    if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        folderToSaveIn = Paths.get(fc.getSelectedFile().getPath(), product.getName()).toString();

                        isSaved = true;
                        putValue(Action.NAME, "Save product");

                        productPersistor.allocateForPersistence();
                        save();
                    }
                } else {
                    save();
                }
            }

            private void save() {
                product.save();
            }
        };

        saveProductAction.setEnabled(false);

        toolBar.add(new AbstractAction("New product...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productName = JOptionPane.showInputDialog(frame, "Product name");

                saveProductAction.putValue(Action.NAME, "Save product...");
                saveProductAction.setEnabled(true);

                product = new Product(productName, productPersistor, layerFactory);

                tabbedPane.removeAll();
                root.removeAllChildren();
                treeModel.nodeChanged(root);

                splitPane.setLeftComponent(product.toOverview(tree, treeModel, root, overview));

                splitPane.setDividerLocation(200);

                isSaved = false;
            }
        });

        toolBar.add(new AbstractAction("Open product...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProductAction.putValue(Action.NAME, "Save product");

                JFileChooser fc = new JFileChooser();

                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    folderToSaveIn = fc.getSelectedFile().getPath();
                    saveProductAction.setEnabled(true);
                    isSaved = true;

                    String productName = Paths.get(folderToSaveIn).getName(Paths.get(folderToSaveIn).getNameCount() - 1).toString();
                    product = new Product(productName, productPersistor, layerFactory);

                    String source = null;
                    try {
                        String fileToSaveIn = Paths.get(folderToSaveIn, "product").toString();
                        source = new String(java.nio.file.Files.readAllBytes(Paths.get(fileToSaveIn)));
                        ScriptEngineManager engineManager = new ScriptEngineManager();
                        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
                        engine.put("openLayer", (Consumer<String>) s -> {
                            product.openLayer(s);
                        });
                        isLoading = true;
                        engine.eval(source);
                        isLoading = false;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (ScriptException e1) {
                        e1.printStackTrace();
                    }

                    tabbedPane.removeAll();
                    root.removeAllChildren();
                    treeModel.nodeChanged(root);

                    splitPane.setLeftComponent(product.toOverview(tree, treeModel, root, overview));

                    splitPane.setDividerLocation(200);
                }
            }
        });

        // http://stackoverflow.com/questions/17863179/how-do-i-make-jtree-stop-cell-editing-when-either-focus-lost-or-left-click-occu
        // Could be interesting for handling node editing/cancel editing when focus is lost
        toolBar.add(new AbstractAction("Add Layer") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode newLayerNode = new DefaultMutableTreeNode();
                treeModel.insertNodeInto(newLayerNode, root, root.getChildCount());

                editNode(tree, new TreePath(treeModel.getPathToRoot(newLayerNode)), () -> {
                    treeModel.removeNodeFromParent(newLayerNode);
                }, () -> {
                    String layerName = (String)((DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent()).getUserObject();
                    treeModel.removeNodeFromParent(newLayerNode);

                    if(layerName.length() > 0) {
                        product.addLayer(layerName);
                    }
                });
            }
        });

        toolBar.add(saveProductAction);

        Action renameLayer = new AbstractAction("Rename layer") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
                Layer layer = (Layer) layerNode.getUserObject();

                editNode(tree, tree.getSelectionPath(), () -> {

                },  () -> {
                    String layerName = (String)((DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent()).getUserObject();

                    if(layerName.length() > 0) {
                        layer.setName(layerName);
                    }

                    layerNode.setUserObject(layer);
                    treeModel.nodeChanged(layerNode);
                });
            }
        };
        renameLayer.setEnabled(false);

        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                if(selectedNode != null) {
                    renameLayer.setEnabled(selectedNode.getUserObject() instanceof Layer);
                }
            }
        });

        toolBar.add(renameLayer);

        Action moveUpLayer = new AbstractAction("Move up layer") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
                Layer layer = (Layer) layerNode.getUserObject();

                product.moveUpLayer(layer);
            }
        };
        moveUpLayer.setEnabled(false);

        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                if(selectedNode != null) {
                    if(selectedNode.getUserObject() instanceof Layer) {
                        Layer layer = (Layer) selectedNode.getUserObject();
                        moveUpLayer.setEnabled(!product.isFirstLayer(layer));
                    }
                }
            }
        });

        toolBar.add(moveUpLayer);

        Action moveDownLayer = new AbstractAction("Move down layer") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
                Layer layer = (Layer) layerNode.getUserObject();

                product.moveDownLayer(layer);
            }
        };
        moveDownLayer.setEnabled(false);

        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                if(selectedNode != null) {
                    if(selectedNode.getUserObject() instanceof Layer) {
                        Layer layer = (Layer) selectedNode.getUserObject();
                        moveDownLayer.setEnabled(!product.isLastLayer(layer));
                    }
                }
            }
        });

        toolBar.add(moveDownLayer);

        toolBar.add(new AbstractAction("Add class") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode newClassNode = new DefaultMutableTreeNode();
                DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
                Layer layer = (Layer) layerNode.getUserObject();
                treeModel.insertNodeInto(newClassNode, layerNode, layerNode.getChildCount());

                editNode(tree, new TreePath(treeModel.getPathToRoot(newClassNode)), () -> {
                    treeModel.removeNodeFromParent(newClassNode);

                    tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(layerNode)));
                }, () -> {
                    String className = (String)((DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent()).getUserObject();
                    treeModel.removeNodeFromParent(newClassNode);

                    if(className.length() > 0) {
                        try {
                            layer.appendSource("addClass('" + className + "')");
                        } catch (ScriptException e1) {
                            e1.printStackTrace();
                        }
                    }

                    tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(layerNode)));
                });
            }
        });

        contentPane.add(toolBar, BorderLayout.NORTH);

        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                SwingWorker swingWorker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        ScriptEngineManager engineManager = new ScriptEngineManager();
                        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");
                        try {
                            engine.eval("var dummy = 12345");
                        } catch (ScriptException e1) {
                            e1.printStackTrace();
                        }

                        return null;
                    }
                };
                swingWorker.execute();
            }
        });
    }

    private static void editNode(JTree tree, TreePath selectionPath, Runnable editingCanceled, Runnable editingComitted) {
        tree.setEditable(true);
        tree.startEditingAtPath(selectionPath);
        tree.getCellEditor().addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                tree.getCellEditor().removeCellEditorListener(this);
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                tree.getCellEditor().removeCellEditorListener(this);
                tree.setEditable(false);
                editingCanceled.run();
            }
        });
        tree.getModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                tree.getModel().removeTreeModelListener(this);

                editingComitted.run();

                tree.setEditable(false);
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {

            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {

            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {

            }
        });
    }
}
