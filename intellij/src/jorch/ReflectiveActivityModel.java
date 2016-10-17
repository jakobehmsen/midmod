package jorch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ReflectiveActivityModel implements ActivityModel {
    private DependencyInjector dependencyInjector;
    private Class<? extends Step> c;

    public ReflectiveActivityModel(DependencyInjector dependencyInjector, Class<? extends Step> c) {
        this.dependencyInjector = dependencyInjector;
        this.c = c;
    }

    @Override
    public String toString() {
        return c.getName();
    }

    @Override
    public Step toStep() {
        try {
            Constructor<? extends Step> constructor = (Constructor<? extends Step>) c.getConstructors()[0];
            Object[] arguments = new Object[constructor.getParameterCount()];
            for(int i = 0; i < constructor.getParameterCount(); i++) {
                Object instance = dependencyInjector.getInstance(constructor.getParameterTypes()[i]);
                arguments[i] = instance;
            }

            Step step = constructor.newInstance(arguments);
            return step;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
