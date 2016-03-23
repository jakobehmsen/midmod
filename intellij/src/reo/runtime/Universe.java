package reo.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
    private DeltaRObject instructions;

    public Universe() {
        anyPrototype = new DeltaRObject(null);
        anyPrototype.put("Any", anyPrototype);
        integerPrototype = createPrototype("Integer");
        arrayPrototype = createPrototype("Array");
        functionPrototype = createPrototype("Function");
        aNull = createPrototype("Null");
        stringPrototype = createPrototype("String");
        doublePrototype = createPrototype("Double");
        instructions = createPrototype("Instructions");
        Arrays.asList(Instructions.class.getDeclaredMethods()).forEach(m -> {
            DeltaRObject instructionPrototype = new DeltaRObject(anyPrototype);
            instructions.put(m.getName(), instructionPrototype);
            instructionPrototypes.put(m.getName(), instructionPrototype);
        });
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
        return instructionPrototypes.get(instructionName);
    }

    private static class CachedInstruction {
        private String name;
        private Object[] arguments;

        private CachedInstruction(String name, Object[] arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        @Override
        public int hashCode() {
            return name.hashCode() + Arrays.hashCode(arguments);
        }

        @Override
        public boolean equals(Object obj) {
            return this.name.equals(((CachedInstruction)obj).name) &&
                Arrays.equals(this.arguments, ((CachedInstruction)obj).arguments);
        }
    }

    private Hashtable<CachedInstruction, InstructionRObject> cachedInstructions = new Hashtable<>();

    public InstructionRObject getInstruction(String instructionName, Object[] arguments) {
        return cachedInstructions.computeIfAbsent(new CachedInstruction(instructionName, arguments), in -> {
            Method instructionCreator = Arrays.asList(Instructions.class.getDeclaredMethods()).stream()
                .filter(x -> x.getName().equals(instructionName)).findFirst().get();

            try {
                return new InstructionRObject(instructionName, (Instruction) instructionCreator.invoke(null, arguments));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            return null;
        });
    }
}
