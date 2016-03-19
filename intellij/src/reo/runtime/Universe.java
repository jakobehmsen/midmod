package reo.runtime;

import java.util.Hashtable;

public class Universe {
    private DeltaRObject anyPrototype;
    private DeltaRObject integerPrototype;
    private DeltaRObject arrayPrototype;
    private DeltaRObject functionPrototype;
    private DeltaRObject aNull;
    private DeltaRObject stringPrototype;
    private DeltaRObject doublePrototype;
    private Hashtable<String, DeltaRObject> instructionPrototypes = new Hashtable<>();

    public Universe() {
        anyPrototype = new DeltaRObject(null);
        anyPrototype.put("Any", anyPrototype);
        anyPrototype.put("getSlot/1", new FunctionRObject(new Behavior(new Instruction[]{
            Instructions.loadLocal(0),
            Instructions.loadLocal(1),
            Instructions.loadSlot(),
            Instructions.ret()
        })));
        anyPrototype.put("putSlot/2", new FunctionRObject(new Behavior(new Instruction[]{
            Instructions.loadLocal(0),
            Instructions.loadLocal(1),
            Instructions.loadLocal(2),
            Instructions.storeSlot(),
            Instructions.ret()
        })));
        integerPrototype = createPrototype("Integer");
        arrayPrototype = createPrototype("Array");
        functionPrototype = createPrototype("Function");
        aNull = createPrototype("Null");
        stringPrototype = createPrototype("String");
        doublePrototype = createPrototype("Double");
    }

    private DeltaRObject createPrototype(String name) {
        DeltaRObject prototype = new DeltaRObject(anyPrototype);
        anyPrototype.put(name, prototype);
        return prototype;
    }

    public RObject getIntegerPrototype() {
        return integerPrototype;
    }

    public RObject evaluate(Behavior behavior, RObject receiver) {
        Evaluation evaluation = new Evaluation(this);
        evaluation.setFrame(behavior.createFrame(null));
        evaluation.getFrame().push(receiver);
        evaluation.evaluate();
        return evaluation.getFrame().peek();
    }

    public RObject getArrayPrototype() {
        return arrayPrototype;
    }

    public RObject getFunctionPrototype() {
        return functionPrototype;
    }

    public RObject getNull() {
        return aNull;
    }

    public RObject getStringPrototype() {
        return stringPrototype;
    }

    public DeltaRObject getAnyPrototype() {
        return anyPrototype;
    }

    public RObject getDoublePrototype() {
        return doublePrototype;
    }

    public RObject getInstructionPrototype(String instructionName) {
        return instructionPrototypes.computeIfAbsent(instructionName, in -> new DeltaRObject(anyPrototype));
    }
}
