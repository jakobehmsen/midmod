package reo.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public abstract class ReflectiveObserver implements Observer {
    @Override
    public void handle(Object change) {
        Class<?> changeClass = change.getClass();
        while(changeClass != null) {
            Class<?> currentChangeClass = changeClass;
            Optional<Method> foundMethod = Arrays.asList(getClass().getDeclaredMethods()).stream()
                .filter(x ->
                        x.getName().equals("handle") &&
                            x.getParameterTypes().length == 1 &&
                            x.getParameterTypes()[0].equals(currentChangeClass)
                ).findFirst();
            if (foundMethod.isPresent()) {
                try {
                    foundMethod.get().setAccessible(true);
                    foundMethod.get().invoke(this, change);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            changeClass = changeClass.getSuperclass();
        }
    }
}
