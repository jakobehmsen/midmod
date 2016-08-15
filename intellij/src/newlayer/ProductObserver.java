package newlayer;

public interface ProductObserver {
    void addedLayer(Product product, Layer layer, int index);
    void removedLayer(Product product, Layer layer, int index);
}
