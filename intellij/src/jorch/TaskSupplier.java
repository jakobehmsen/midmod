package jorch;

import java.util.function.Consumer;

public interface TaskSupplier {
    Consumer<Token> newTask();
}
