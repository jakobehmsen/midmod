package newlayer;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
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
    private static String folderToSaveIn;

    public static void main(String[] args) throws ScriptException {
        LayerFactory layerFactory = new LayerFactory() {
            @Override
            public Layer createLayer(String name) {
                return new Layer(new LayerPersistor() {
                    @Override
                    public void save(Layer layer) {
                        String layerFile = getLayerFile(name);

                        try {
                            java.nio.file.Files.write(Paths.get(layerFile), layer.getSource().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, name);
            }

            private String getLayerFile(String name) {
                return Paths.get(folderToSaveIn, "layers", name + ".layer").toString();
            }

            @Override
            public Layer openLayer(String name) {
                Layer layer = createLayer(name);

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
            @Override
            public void saveProduct(Product product) {
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
            }

            @Override
            public void allocateForPersistence() {
                if(!new File(folderToSaveIn).exists())
                    new File(folderToSaveIn).mkdir();

                layerFactory.allocateForPersistence();
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
            private Hashtable<Resource, JComponent> openedResources = new Hashtable<>();

            @Override
            public void open(Resource resource) {
                if(openedResources.containsKey(resource)) {
                    JComponent resourceView = openedResources.get(resource);
                    int tabIndex = tabbedPane.indexOfComponent(resourceView);
                    tabbedPane.setSelectedIndex(tabIndex);
                } else {
                    JComponent resourceView = resource.toView();
                    tabbedPane.add(resource.getPath(), resourceView);
                    openedResources.put(resource, resourceView);
                    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                }
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
                        engine.eval(source);
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

        toolBar.add(new AbstractAction("Add Layer...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String layerName = JOptionPane.showInputDialog(frame, "Layer name");

                product.addLayer(layerName);
            }
        });

        toolBar.add(saveProductAction);

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
}
