package jorch;

import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultTaskFactory implements TaskFactory {
    private Map<String, Function<Object[], Consumer<Token>>> mappings = new Hashtable<>();

    public void mapTask(String name, Function<Object[], Consumer<Token>> mapping) {
        mappings.put(name, mapping);
    }

    @Override
    public Consumer<Token> newTask(String name, Object[] arguments) {
        return mappings.get(name).apply(arguments);
    }
}
