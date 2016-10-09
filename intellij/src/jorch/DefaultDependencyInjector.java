package jorch;

import java.util.Hashtable;
import java.util.Map;

public class DefaultDependencyInjector implements DependencyInjector {
    private Map<Class<?>, Object> instances;

    public DefaultDependencyInjector() {
        instances = new Hashtable<>();
    }

    public void put(Class<?> c, Object instance) {
        instances.put(c, instance);
    }

    @Override
    public Object getInstance(Class<?> c) {
        return instances.computeIfAbsent(c, k -> {
            try {
                return c.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return null;
        });
    }
}
