package newlayer;

public interface LayerObserver {
    void wasUpdated(Layer layer);
    void requestUpdate(Layer layer);
}
