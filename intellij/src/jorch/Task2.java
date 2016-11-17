package jorch;

public interface Task2<T> {
    T perform(TaskScheduler scheduler);
}
