package jorch;

import java.util.function.Consumer;

public interface SequentialScheduler extends AutoCloseable {
    EventHandlerContainer getEventHandlerContainer();
    void finish(Object result);
    void scheduleNext(Consumer<SequentialScheduler> nextTask);
    ConcurrentScheduler split();
    Object getResult();
}
