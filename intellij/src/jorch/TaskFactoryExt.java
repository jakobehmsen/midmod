package jorch;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskFactoryExt {
    private DefaultTaskFactory taskFactory;
    private Function<Token, Scheduler> schedulerSupplier;

    public TaskFactoryExt(DefaultTaskFactory taskFactory, Function<Token, Scheduler> schedulerSupplier) {
        this.taskFactory = taskFactory;
        this.schedulerSupplier = schedulerSupplier;
    }

    public void mapTask(String name, Function<Object[], Consumer<Token>> mapping) {
        taskFactory.mapTask(name, mapping);
    }

    public void mapSplit(String name, Function<Object[], BiConsumer<Token, Scheduler>> mapping) {
        mapTask(name, arguments -> {
            BiConsumer<Token, Scheduler> splitTask = mapping.apply(arguments);
            return token -> {
                Scheduler scheduler = schedulerSupplier.apply(token);
                splitTask.accept(token, scheduler);
            };
        });
    }
}
