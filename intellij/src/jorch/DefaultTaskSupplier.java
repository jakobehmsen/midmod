package jorch;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultTaskSupplier implements TaskSupplier, Serializable {
    private Class<? extends Consumer<Token>> c;
    private Constructor<? extends Consumer<Token>> constructor;
    private Class<?>[] parameterTypes;
    private Object[] constructorArgs;

    public DefaultTaskSupplier(Class<? extends Consumer<Token>> c, Class<?>[] parameterTypes, Object[] constructorArgs) {
        this.c = c;
        this.parameterTypes = parameterTypes;
        try {
            this.constructor = c.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        this.constructorArgs = constructorArgs;
    }

    public <T, R extends T> DefaultTaskSupplier withArgument(Class<T> parameterType, R argument) {
        Class<?>[] parameters = new Class<?>[parameterTypes.length + 1];
        System.arraycopy(parameterTypes, 0, parameters, 0, parameterTypes.length);
        parameters[parameters.length - 1] = parameterType;
        Object[] arguments = new Object[constructorArgs.length + 1];
        System.arraycopy(constructorArgs, 0, arguments, 0, constructorArgs.length);
        arguments[arguments.length - 1] = argument;
        return new DefaultTaskSupplier(c, parameters, arguments);
    }

    public DefaultTaskSupplier(Class<? extends Consumer<Token>> c, Object... constructorArgs) {
        this.c = c;
        this.parameterTypes = Arrays.asList(constructorArgs).stream().map(x -> x.getClass()).toArray(s -> new Class<?>[s]);
        try {
            this.constructor = c.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        }
        this.constructorArgs = constructorArgs;
    }

    @Override
    public Consumer<Token> newTask() {
        replace(constructor.getDeclaringClass(), constructorArgs);

        try {
            return constructor.newInstance(constructorArgs);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void replace(Class<? extends Consumer<Token>> cOrig, Object[] origArguments) {
        try {
            ClassReplacer classReplacer = new ClassReplacer() {
                @Override
                public void replaceWith(Class<? extends Consumer<Token>> c, Object[] arguments) {
                    try {
                        constructor = c.getConstructor(Arrays.asList(constructorArgs).stream().map(x -> x.getClass()).toArray(s -> new Class<?>[s]));
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    constructorArgs = arguments;

                    replace(c, arguments);
                }
            };
            List<Object> replaceArguments = Stream.concat(Arrays.asList(classReplacer).stream(), Arrays.asList(constructorArgs).stream()).collect(Collectors.toList());
            List<Object> replaceParameterTypes = Stream.concat(Arrays.asList(ClassReplacer.class).stream(), Arrays.asList(constructorArgs).stream().map(x -> x.getClass())).collect(Collectors.toList());
            Method replaceWithMethod = cOrig.getMethod("replaceWith", replaceParameterTypes.stream().toArray(s -> new Class<?>[s]));
            replaceWithMethod.invoke(null, replaceArguments.stream().toArray(s -> new Object[s]));

        } catch (NoSuchMethodException e) {

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return constructor.getDeclaringClass().getName();
    }

    private void writeObject(java.io.ObjectOutputStream stream)
        throws IOException {
        stream.writeObject(constructorArgs);
        stream.writeObject(parameterTypes);
        stream.writeUTF(c.getName());
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        constructorArgs = (Object[])stream.readObject();
        parameterTypes = (Class<?>[])stream.readObject();
        String className = stream.readUTF();
        c = (Class<? extends Consumer<Token>>) Class.forName(className);
        try {
            this.constructor = c.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
