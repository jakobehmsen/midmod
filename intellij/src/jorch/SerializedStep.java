package jorch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SerializedStep implements Step {
    private DependencyInjector dependencyInjector;
    private Class<? extends Step> c;
    private Step step;

    public SerializedStep(DependencyInjector dependencyInjector, Class<? extends Step> c, Step step) {
        this.dependencyInjector = dependencyInjector;
        this.c = c;
        this.step = step;
    }

    private void writeObject(ObjectOutputStream oos)
        throws IOException {
        oos.writeUTF(c.getName());
    }

    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        String className = ois.readUTF();
        c = (Class<? extends Step>) Class.forName(className);
        dependencyInjector = DependencyInjectorContext.getInstance();

        step = load();
    }

    public Step load() {
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

    @Override
    public void perform(Token token) {
        step.perform(token);
    }

    @Override
    public String toString() {
        return step.toString();
    }
}
