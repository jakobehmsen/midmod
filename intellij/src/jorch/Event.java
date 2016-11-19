package jorch;

public interface Event<T> {
    Class<T> eventHandlerType();
    void beHandledBy(T eventHandler);
}
