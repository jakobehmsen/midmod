package jorch;

public interface Token extends AutoCloseable {
    EventChannel getEventChannel();
    void finish(Object result);
    void passTo(TaskSelector nextTask);
    Token newToken(TaskSelector initialTask);
    Object getResult();
    Token getParent();
}
