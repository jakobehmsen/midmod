package jorch;

public interface SequentialSchedulerEventHandler {
    void proceeded();
    void finished();
    void wasClosed();
}
