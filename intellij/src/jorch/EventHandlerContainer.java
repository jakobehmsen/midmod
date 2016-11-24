package jorch;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class EventHandlerContainer {
    private ArrayList<Object> eventHandlers = new ArrayList<>();

    public <T extends Object> void addEventHandler(T eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public <T extends Object> void removeEventHandler(T eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public void fireEvent(Event event) {
        eventHandlers.stream().filter(h -> event.eventHandlerType().isInstance(h)).collect(Collectors.toList()).forEach(h -> event.beHandledBy(h));
    }
}
