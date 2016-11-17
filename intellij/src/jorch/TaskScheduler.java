package jorch;

public interface TaskScheduler {
    TaskScheduler fork();
    <T> TaskFuture<T> schedule(Task2<T> runnable);
    void proceed();
    boolean hasMore();
    void merge();
}
