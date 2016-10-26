package jorch;

public interface Token {
    void perform(Step step, Step callback);
    void moveNext();
    Step currentStep();
    void halt();
    void proceed();
    void put(String name, Object value);
}
