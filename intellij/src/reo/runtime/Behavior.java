package reo.runtime;

public class Behavior {
    private Instruction[] instructions;

    public Behavior(Instruction[] instructions) {
        this.instructions = instructions;
    }

    public Frame createFrame(Frame outer) {
        return new Frame(outer, instructions);
    }
}
