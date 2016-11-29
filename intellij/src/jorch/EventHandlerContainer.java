package jorch;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EventHandlerContainer {
    private ArrayList<Object> eventHandlers = new ArrayList<>();

    public <T extends Object> void addEventHandler(T eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public <T extends Object> void removeEventHandler(T eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public <T> void fireEvent(Class<T> eventHandlerType, Consumer<T> eventFirer) {
        eventHandlers.stream().filter(h -> eventHandlerType.isInstance(h)).collect(Collectors.toList()).forEach(h -> eventFirer.accept((T)h));
    }
}
