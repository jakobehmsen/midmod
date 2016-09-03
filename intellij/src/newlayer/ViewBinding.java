package newlayer;

public interface ViewBinding<T> {
    T getView();
    void remove();

    void select(NodeInfo nodeInfo);
}
