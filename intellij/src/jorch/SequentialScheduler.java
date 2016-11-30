package jorch;

import java.util.function.Consumer;

public interface SequentialScheduler extends AutoCloseable {
    EventHandlerContainer getEventHandlerContainer();
    void finish(Object result);
    void scheduleNext(Consumer<SequentialScheduler> nextTask);
    SequentialScheduler newSequentialScheduler(Consumer<SequentialScheduler> initialTask);
    Object getResult();
    SequentialScheduler getParent();
}