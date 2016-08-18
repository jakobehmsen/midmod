package newlayer;

public interface LayerFactory {
    Layer createLayer(String name);
    Layer openLayer(String name);
}
