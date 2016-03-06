package reo.runtime;

public interface Expression {
    RObject perform(Evaluation evaluation);
}
