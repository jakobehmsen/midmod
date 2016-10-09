package jorch;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Map;

public class JavaStep implements Step {
    private DependencyInjector dependencyInjector;
    private Class<? extends Step> c;

    public JavaStep(DependencyInjector dependencyInjector, Class<? extends Step> c) {
        this.dependencyInjector = dependencyInjector;
        this.c = c;
    }

    @Override
    public void perform(Token token, Map<String, Object> context) {
        try {
            Constructor<? extends Step> constructor = (Constructor<? extends Step>) c.getConstructors()[0];
            Object[] arguments = new Object[constructor.getParameterCount()];
            for(int i = 0; i < constructor.getParameterCount(); i++) {
                Object instance = dependencyInjector.getInstance(constructor.getParameterTypes()[i]);
                arguments[i] = instance;
            }

            Step step = constructor.newInstance(arguments);
            step.perform(token, context);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
