package jorch;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EventChannel {
    private ArrayList<Object> listeners = new ArrayList<>();

    public <T extends Object> void add(T listener) {
        listeners.add(listener);
    }

    public <T extends Object> void remove(T listener) {
        listeners.add(listener);
    }

    public <T> void fireEvent(Class<T> listenerType, Consumer<T> eventFirer) {
        listeners.stream().filter(h -> listenerType.isInstance(h)).collect(Collectors.toList()).forEach(h -> eventFirer.accept((T)h));
    }
}
