package jorch;

import java.util.function.Consumer;

public interface LoadStrategy {
    Consumer<SequentialScheduler> load(Consumer<SequentialScheduler> task);
}
