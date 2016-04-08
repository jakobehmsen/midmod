package reo.runtime;

public class Behavior {
    private Instruction[] instructions;
    private int internalCount;
    private int externalCount;

    public Behavior(Instruction[] instructions, int internalCount, int externalCount) {
        this.instructions = instructions;
        this.internalCount = internalCount;
        this.externalCount = externalCount;
    }

    public Frame createFrame(Frame outer, Observable[] arguments) {
        Frame frame = new Frame(outer, instructions);
        frame.allocate(internalCount + externalCount);
        frame.set(arguments, 0);
        return frame;
    }
}
