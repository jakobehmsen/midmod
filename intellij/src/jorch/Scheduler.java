package jorch;

public interface Scheduler {
    void perform(Task task);
    void halt(Task task);
    void finish();
    void resume();
}
