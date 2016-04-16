package reo.runtime;

public interface Observer {
    default void initialize() { }
    void handle(Object value);
    default void release() { }
    //void release();
}
