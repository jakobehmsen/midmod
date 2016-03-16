package reo.runtime;

public class Universe {
    private CustomRObject anyPrototype;
    private CustomRObject integerPrototype;
    private CustomRObject arrayPrototype;
    private CustomRObject functionPrototype;
    private CustomRObject aNull;
    private CustomRObject stringPrototype;
    private CustomRObject doublePrototype;

    public Universe() {
        anyPrototype = new CustomRObject(null);
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

    private CustomRObject createPrototype(String name) {
        CustomRObject prototype = new CustomRObject(anyPrototype);
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

    public CustomRObject getAnyPrototype() {
        return anyPrototype;
    }

    public RObject getDoublePrototype() {
        return doublePrototype;
    }
}
