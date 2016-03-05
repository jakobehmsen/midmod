package midmod.parse;

public interface TransationSupport {
    void begin();
    void commit();
    void rollback();
}
