package reo.runtime;

public interface Instruction {
    default boolean isFunctional() { return false; }
    void evaluate(Evaluation evaluation);
}
