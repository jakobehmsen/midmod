package chasm;

public interface AspectSession {
    void processNext(ChangeStatement element);
    void close();
}
