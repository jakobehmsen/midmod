package newlayer;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptEngineManager;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Product implements LayerObserver {
    private ProductPersistor productPersistor;
    private LayerFactory layerFactory;
    private ArrayList<Layer> layers = new ArrayList<>();
    private String name;

    public Product(String name, ProductPersistor productPersistor, LayerFactory layerFactory) {
        this.name = name;
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

        productPersistor.addedLayer(this, layer, index);
        observers.forEach(o -> o.addedLayer(this, layer, index));

        updateFromLayer(layer);
    }

    public void insertLayer(String name, int index) {
        insertLayer(layerFactory.createLayer(name), index);
    }

    @Override
    public void outputUpdated(Layer layer) {

    }

    @Override
    public void transformationChanged(Layer layer) {
        updateFromLayer(layer);
    }

    @Override
    public void nameChanged(Layer layer) {
        productPersistor.layerNameChanged(layer);
    }

    public void removeLayer(String name) {
        int indexOfLayer = IntStream.range(0, layers.size()).filter(i -> layers.get(i).getName().equals(name)).findFirst().orElse(-1);
        if(indexOfLayer != -1) {
            Layer layer = layers.get(indexOfLayer);
            removeLayer(layer);
        }
    }

    public void removeLayer(Layer layer) {
        int indexOfLayer = layers.indexOf(layer);
        if(indexOfLayer != -1) {
            layer.removeObserver(this);
            layers.remove(indexOfLayer);

            productPersistor.removedLayer(this, layer, indexOfLayer);
            observers.forEach(o -> o.removedLayer(this, layer, indexOfLayer));

            if(layers.size() > indexOfLayer) {
                updateFromLayer(layers.get(indexOfLayer));
            }
        }
    }

    private void updateFromLayer(Layer layer) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");

        Layer[] previousLayerHolder = new Layer[1];
        Layer[] currentLayer = new Layer[1];
        List<ClassResource>[] classes = new List[1];

        engine.put("addClass", (Consumer<String>) s -> {
            classes[0].add(new ClassResource(currentLayer[0], s));
        });
        engine.put("getClass", (Function<String, ClassResource>) s -> {
            return classes[0].stream().filter(x -> x.getName().equals(s)).findFirst().get();
        });
        engine.put("getClasses", (Supplier<List<ClassResource>>) () -> {
            return classes[0].stream().collect(Collectors.toList());
        });
        engine.put("parameter", (BiFunction<String, String, ClassResource.ParameterInfo>) (typeName, name) -> {
            return new ClassResource.ParameterInfo(typeName, name);
        });

        for(int indexOfLayer = 0; indexOfLayer < layers.size(); indexOfLayer++) {
            layer = layers.get(indexOfLayer);
            currentLayer[0] = layer;
            if (indexOfLayer == 0) {
                classes[0] = new ArrayList();

                layer.transform(engine);
                layer.setClasses(classes[0]);
            } else {
                List<ClassResource> innerLayerClasses = classes[0];
                classes[0] = new ArrayList();

                innerLayerClasses.forEach(x -> {
                    classes[0].add(x.copy(currentLayer[0]));
                });

                Layer previousLayer = layers.get(indexOfLayer - 1);
                previousLayerHolder[0] = previousLayer;
                layer.transform(engine);
                layer.setClasses(classes[0]);
            }
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
        layers.forEach(x -> {
            DefaultMutableTreeNode layerNode = (DefaultMutableTreeNode) x.toTreeNode(treeModel, overview);
            treeModel.insertNodeInto(layerNode, root, root.getChildCount());
        });
        treeModel.nodeStructureChanged(root);

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

    public String getName() {
        return name;
    }

    public void moveUpLayer(Layer layer) {
        int indexOfLayer = layers.indexOf(layer);
        removeLayer(layer);
        insertLayer(layer, indexOfLayer - 1);
    }

    public void moveDownLayer(Layer layer) {
        int indexOfLayer = layers.indexOf(layer);
        removeLayer(layer);
        insertLayer(layer, indexOfLayer + 1);
    }

    public boolean isFirstLayer(Layer layer) {
        return layers.indexOf(layer) == 0;
    }

    public boolean isLastLayer(Layer layer) {
        return layers.indexOf(layer) == layers.size() - 1;
    }
}
