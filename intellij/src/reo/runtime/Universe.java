package reo.runtime;

public class Universe {
    private CustomRObject anyPrototype;
    private CustomRObject integerPrototype;
    private CustomRObject arrayPrototype;
    private CustomRObject functionPrototype;
    private CustomRObject aNull;
    private CustomRObject stringPrototype;

    public Universe() {
        anyPrototype = new CustomRObject();
        anyPrototype.put("getSlot", new FunctionRObject(new Behavior(new Instruction[]{
            Instructions.loadLocal(0),
            Instructions.loadLocal(1),
            Instructions.loadSlot(),
            Instructions.ret()
        })));
        anyPrototype.put("putSlot", new FunctionRObject(new Behavior(new Instruction[]{
            Instructions.loadLocal(0),
            Instructions.loadLocal(1),
            Instructions.loadLocal(2),
            Instructions.storeSlot(),
            Instructions.ret()
        })));
        integerPrototype = new CustomRObject();
        integerPrototype.putPrototype("proto", anyPrototype);
        integerPrototype.put("+", new FunctionRObject(new Behavior(new Instruction[]{
            Instructions.loadLocal(0),
            Instructions.loadLocal(1),
            Instructions.addi(),
            Instructions.ret()
        })));
        arrayPrototype = new CustomRObject();
        arrayPrototype.putPrototype("proto", anyPrototype);
        functionPrototype = new CustomRObject();
        functionPrototype.putPrototype("proto", anyPrototype);
        aNull = new CustomRObject();
        aNull.putPrototype("proto", anyPrototype);
        stringPrototype = new CustomRObject();
        stringPrototype.putPrototype("proto", anyPrototype);
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
}
