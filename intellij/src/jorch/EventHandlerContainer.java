package jorch;

import java.util.ArrayList;

public class EventHandlerContainer {
    private ArrayList<Object> eventHandlers = new ArrayList<>();

    public <T extends Object> void addEventHandler(T eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public <T extends Object> void removeEventHandler(T eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public void fireEvent(Event event) {
        eventHandlers.stream().filter(h -> event.eventHandlerType().isInstance(h)).forEach(h -> event.beHandledBy(h));
    }
}
