package jorch;

public interface Token extends AutoCloseable {
    EventChannel getEventChannel();
    void finish(Object result);
    void passTo(TaskSupplier nextTask);
    Token newToken(TaskSupplier initialTask);
    Object getResult();
    Token getParent();
}
