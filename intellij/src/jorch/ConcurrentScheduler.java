package jorch;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ConcurrentScheduler extends AutoCloseable {
    <T> TaskFuture<T> call(Consumer<SequentialScheduler> task);
}
