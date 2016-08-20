package newlayer;

public interface LayerObserver {
    void outputUpdated(Layer layer);
    void transformationChanged(Layer layer);
}
