package reo_OLD.runtime;

public class InstructionRObject extends PrimitiveRObject {
    private String name;
    private Instruction instruction;

    public InstructionRObject(String name, Instruction instruction) {
        this.name = name;
        this.instruction = instruction;
    }

    @Override
    protected RObject getPrototype(Universe universe) {
        return universe.getInstructionPrototype(name);
    }

    public Instruction getValue() {
        return instruction;
    }
}
