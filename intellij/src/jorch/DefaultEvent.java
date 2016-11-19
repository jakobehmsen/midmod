package jorch;

public abstract class DefaultEvent<T> implements Event<T> {
    private Class<T> eventHandlerType;

    public DefaultEvent(Class<T> eventHandlerType) {
        this.eventHandlerType = eventHandlerType;
    }

    @Override
    public Class<T> eventHandlerType() {
        return eventHandlerType;
    }
}
