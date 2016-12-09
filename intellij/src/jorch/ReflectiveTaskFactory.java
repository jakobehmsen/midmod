package jorch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReflectiveTaskFactory implements TaskFactory {
    private Function<Token, Scheduler> schedulerSupplier;
    private Object source;

    public ReflectiveTaskFactory(Function<Token, Scheduler> schedulerSupplier, Object source) {
        this.schedulerSupplier = schedulerSupplier;
        this.source = source;
    }

    @Override
    public Consumer<Token> newTask(String name, Object[] arguments) {
        Method resolvedMethod = Arrays.asList(source.getClass().getMethods()).stream().filter(method -> method.getName().equals(name)).findFirst().get();

        if(resolvedMethod.getParameterCount() == arguments.length) {
            return token -> {
                try {
                    Object result = resolvedMethod.invoke(source, arguments);
                    token.finish(result);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
        } else if(resolvedMethod.getParameterCount() == arguments.length + 1) {
            return token -> {
                try {
                    Object[] newArguments = new Object[arguments.length + 1];
                    System.arraycopy(arguments, 0, newArguments, 1, arguments.length);
                    newArguments[0] = token;
                    resolvedMethod.invoke(source, newArguments);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
        } else if(resolvedMethod.getParameterCount() == arguments.length + 2) {
            return token -> {
                try {
                    Object[] newArguments = new Object[arguments.length + 2];
                    System.arraycopy(arguments, 0, newArguments, 2, arguments.length);
                    newArguments[0] = token;
                    newArguments[1] = schedulerSupplier.apply(token);
                    resolvedMethod.invoke(source, newArguments);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
        }

        return null;
    }
}
