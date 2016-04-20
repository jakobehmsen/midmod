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

    public Frame createFrame(Frame outer, Object self, Observable[] arguments) {
        Frame frame = new Frame(outer, instructions);
        frame.allocate(internalCount + externalCount);
        frame.set(0, new Constant(self));
        frame.set(arguments, internalCount);
        return frame;
    }

    public Frame createFrame(Frame outer, Observable self, Observable[] arguments) {
        Frame frame = new Frame(outer, instructions);
        frame.allocate(internalCount + externalCount);
        frame.set(0, self);
        frame.set(arguments, internalCount);
        return frame;
    }
}
