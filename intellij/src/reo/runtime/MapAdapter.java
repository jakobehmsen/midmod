package reo.runtime;

import java.awt.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MapAdapter implements Observer {
    private Set<String> slotNames;
    private Map<String, Binding> bindings;
    private Map<String, Object> map;
    private Consumer<Map<String, Object>> consumer;

    public MapAdapter(Set<String> slotNames, Consumer<Map<String, Object>> consumer) {
        this.slotNames = slotNames;
        bindings = new Hashtable<>();
        map = new Hashtable<>();
        this.consumer = consumer;
    }

    public static MapAdapter forColor(Consumer<Color> consumer) {
        return new MapAdapter(
            Arrays.asList("r", "g", "b").stream().collect(Collectors.toSet()),
            m -> consumer.accept(new Color((int)m.get("r"), (int)m.get("g"), (int)m.get("b"))));
    }

    public static MapAdapter forDimension(Consumer<Dimension> consumer) {
        return new MapAdapter(
            Arrays.asList("width", "height").stream().collect(Collectors.toSet()),
            m -> consumer.accept(new Dimension((int)m.get("width"), (int)m.get("height"))));
    }

    @Override
    public void handle(Object value) {
        bindings.entrySet().forEach(x -> x.getValue().remove());
        bindings.clear();

        slotNames.forEach(sn -> {
            Binding binding = ((Dictionary)value).get(sn).bind(new Observer() {
                @Override
                public void handle(Object value) {
                    map.put(sn, value);
                    update();
                }
            });
            bindings.put(sn, binding);
        });
    }

    private void update() {
        if(map.size() == slotNames.size())
            consumer.accept(map);
    }
}
