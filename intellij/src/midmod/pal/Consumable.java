package midmod.pal;

public interface Consumable {
    Object peek();
    void consume();
    boolean atEnd();
    void mark();
    void commit();
    void rollback();
}
