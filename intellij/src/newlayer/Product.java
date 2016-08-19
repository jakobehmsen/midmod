package newlayer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class Product implements LayerObserver {
    private ProductPersistor productPersistor;
    private LayerFactory layerFactory;
    private ArrayList<Layer> layers = new ArrayList<>();

    public Product(ProductPersistor productPersistor, LayerFactory layerFactory) {
        this.productPersistor = productPersistor;
        this.layerFactory = layerFactory;
    }

    public void addLayer(String name) {
        insertLayer(name, layers.size());
    }

    public void addLayer(Layer layer) {
        insertLayer(layer, layers.size());
    }

    public void insertLayer(Layer layer, int index) {
        layers.add(index, layer);
        layer.addObserver(this);

        observers.forEach(o -> o.addedLayer(this, layer, index));

        updateFromLayer(layer);
    }

    public void insertLayer(String name, int index) {
        insertLayer(layerFactory.createLayer(name), index);
    }

    @Override
    public void wasUpdated(Layer layer) {
        int indexOfLayer = layers.indexOf(layer);
        if(indexOfLayer + 1 < layers.size()) {
            Layer nextLayer = layers.get(indexOfLayer + 1);
            nextLayer.updateFrom(layer);
        }
    }

    @Override
    public void requestUpdate(Layer layer) {
        updateFromLayer(layer);
    }

    public void removeLayer(String name) {
        int indexOfLayer = IntStream.range(0, layers.size()).filter(i -> layers.get(i).getName().equals(name)).findFirst().orElse(-1);
        if(indexOfLayer != -1) {
            Layer layer = layers.get(indexOfLayer);
            layer.removeObserver(this);
            layers.remove(indexOfLayer);

            observers.forEach(o -> o.removedLayer(this, layer, indexOfLayer));

            if(layers.size() > indexOfLayer)
                layers.get(indexOfLayer).updateFrom(layers.get(indexOfLayer));
        }
    }

    private void updateFromLayer(Layer layer) {
        int indexOfLayer = layers.indexOf(layer);
        if(indexOfLayer == 0)
            layer.updateFrom(null);
        else {
            Layer previousLayer = layers.get(indexOfLayer - 1);
            layer.updateFrom(previousLayer);
        }
    }

    private ArrayList<ProductObserver> observers = new ArrayList<>();

    public void addObserver(ProductObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ProductObserver observer) {
        observers.remove(observer);
    }

    public JComponent toOverview(JTree tree, DefaultTreeModel treeModel, DefaultMutableTreeNode root, Overview overview) {
        //DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        //DefaultTreeModel treeModel = new DefaultTreeModel(root);

        //JTree tree = new JTree(treeModel);

        /*tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setToggleClickCount(0);*/

        layers.forEach(x -> {
            DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) x.toTreeNode(treeModel, overview);
            treeModel.insertNodeInto(layerNode, root, root.getChildCount());
        });
        treeModel.nodeStructureChanged(root);

        /*tree.addMouseListener(new MouseAdapter() {
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
        });*/

        addObserver(new ProductObserver() {
            @Override
            public void addedLayer(Product product, Layer layer, int index) {
                DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) layer.toTreeNode(treeModel, overview);
                treeModel.insertNodeInto(layerNode, root, index);
                treeModel.nodeStructureChanged(root);
            }

            @Override
            public void removedLayer(Product product, Layer layer, int index) {
                treeModel.removeNodeFromParent((MutableTreeNode) root.getChildAt(index));
                treeModel.nodeStructureChanged(root);
            }
        });

        return new JScrollPane(tree);
    }

    public Layer getLayer(String name) {
        return layers.stream().filter(x -> x.getName().equals(name)).findFirst().get();
    }

    public void save() {
        productPersistor.saveProduct(this);

        layers.forEach(x -> {
            System.out.println("Saving layer " + x);
            x.save();
        });
    }

    public void writeTo(OutputStream output) {
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(output);
        PrintWriter printWriter = new PrintWriter(bufferedOutputStream);

        layers.forEach(x -> {
            printWriter.append("openLayer('" + x.getName() + "')\n");
        });

        printWriter.flush();
    }

    public void openLayer(String name) {
        Layer layer = layerFactory.openLayer(name);
        addLayer(layer);
    }
}
