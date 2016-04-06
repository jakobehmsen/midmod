package reo.runtime;

public interface Observer {
    void handle(Object value);
    default void release() { }
    //void release();
}
