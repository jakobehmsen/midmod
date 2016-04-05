package reo.runtime;

public class Frame {
    private Frame outer;
    private Instruction[] instructions;
    private int ip;

    public Frame(Frame outer, Instruction[] instructions) {
        this.outer = outer;
        this.instructions = instructions;
    }

    public void evaluate(Evaluation evaluation) {
        instructions[ip].evaluate(evaluation);
    }
}
