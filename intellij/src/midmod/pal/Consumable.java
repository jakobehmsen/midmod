package midmod.pal;

import java.util.List;

public interface Consumable {
    Object peek();
    void consume();
    boolean atEnd();
    void mark();
    void commit();
    void rollback();
    void propogate(Object value);
}
