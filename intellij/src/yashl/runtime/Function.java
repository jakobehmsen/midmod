package yashl.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Function {
    private Object value;
    private Instruction[] instructions;

    public Function(Object value) {
        this.value = value;
    }

    public void apply(Evaluation evaluation, Object[] arguments) {
        ensureCompiled();
        Frame callFrame = new Frame(evaluation.getFrame(), instructions);
        for(int i = 0; i < arguments.length; i++)
            callFrame.push(arguments[i]);
        evaluation.setFrame(callFrame);
    }

    private void ensureCompiled() {
        if(instructions == null)
            instructions = compile(value, instructions -> instructions.add(Instructions.ret()));
    }

    public static Instruction[] compile(Object value, Consumer<List<Instruction>> postEmitter) {
        ArrayList<Consumer<List<Instruction>>> emitters = new ArrayList<>();

        if(!(value instanceof ConsCell))
            compileTo(value, emitters, true);
        else {
            ConsCell currentCell = (ConsCell) value;
            while (currentCell != null) {
                boolean asExpression = currentCell.getCdr() == null;
                compileTo(currentCell.getCar(), emitters, asExpression);
                currentCell = currentCell.getCdr();
            }
        }
        ArrayList<Instruction> instructions = new ArrayList<>();
        emitters.forEach(e -> e.accept(instructions));
        postEmitter.accept(instructions);
        return instructions.toArray(new Instruction[instructions.size()]);
    }

    public static void compileTo(Object value, List<Consumer<List<Instruction>>> emitters, boolean asExpression) {
        if(!(value instanceof ConsCell)) {
            if(asExpression) {
                if (value instanceof Symbol) {
                    Symbol symbolValue = (Symbol) value;
                    emitters.add(instructions -> instructions.add(Instructions.getEnvironment(symbolValue)));
                } else
                    emitters.add(instructions -> instructions.add(Instructions.loadConst(value)));
            }
        } else {
            ConsCell listValue = (ConsCell) value;
            Object firstValue = listValue.getCar();
            if(firstValue instanceof Symbol) {
                Symbol symbolFirstValue = (Symbol) firstValue;

                switch (symbolFirstValue.getName()) {
                    case "set":
                        Symbol id = (Symbol) listValue.getCdr().getCar();
                        Object valueForId = listValue.getCdr().getCdr().getCar();
                        compileTo(valueForId, emitters, true);
                        if(asExpression)
                            emitters.add(instructions -> instructions.add(Instructions.dup()));
                        emitters.add(instructions -> instructions.add(Instructions.setEnvironment(id)));

                        break;
                    default: {
                        int arityTmp = 0;
                        ConsCell currentCell = listValue.getCdr();
                        while(currentCell != null) {
                            compileTo(currentCell.getCar(), emitters, true);
                            currentCell = currentCell.getCdr();
                            arityTmp++;
                        }
                        int arity = arityTmp;

                        emitters.add(instructions -> instructions.add(Instructions.call(symbolFirstValue, arity)));
                        if(!asExpression)
                            emitters.add(instructions -> instructions.add(Instructions.pop()));
                    }
                }
            } else {
                compileCons(listValue, emitters);
                if(!asExpression)
                    emitters.add(instructions -> instructions.add(Instructions.pop()));
            }
        }
    }

    private static void compileCons(ConsCell listValue, List<Consumer<List<Instruction>>> emitters) {
        if(listValue.getCdr() == null)
            emitters.add(instructions -> instructions.add(Instructions.loadConst(null)));
        else
            compileCons(listValue.getCdr(), emitters);
        compileTo(listValue.getCar(), emitters, true);
        emitters.add(instructions -> instructions.add(Instructions.cons()));
    }
}
