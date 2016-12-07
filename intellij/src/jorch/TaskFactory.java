package jorch;

import java.util.function.Consumer;

public interface TaskFactory {
    Consumer<Token> newTask(String name, Object[] arguments);
}
