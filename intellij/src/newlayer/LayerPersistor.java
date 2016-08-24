package newlayer;

public interface LayerPersistor {
    void save(Layer layer);
    void changeName(Layer layer, String newName);
    void changedSource(Layer layer);
}
