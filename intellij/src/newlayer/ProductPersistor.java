package newlayer;

public interface ProductPersistor {
    void saveProduct(Product product);
    void allocateForPersistence();

    void layerNameChanged(Layer layer);

    void addedLayer(Product product, Layer layer, int index);

    void removedLayer(Product product, Layer layer, int index);
}
