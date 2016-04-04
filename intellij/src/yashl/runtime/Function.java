package yashl.runtime;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.function.BiConsumer;
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

    private interface SpecialForm {
        void compileTo(List<Consumer<List<Instruction>>> emitters, ConsCell form, boolean asExpression);
    }

    private static Hashtable<Integer, SpecialForm> specialForms = new Hashtable<>();

    static {
        specialForms.put(Symbol.get("set").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands2(emitters, listValue, compileTimeOperand(), runtimeOperand(), (Symbol id, Object valueForId) -> {
                if(asExpression)
                    emitters.add(instructions -> instructions.add(Instructions.dup()));
                emitters.add(instructions -> instructions.add(Instructions.setEnvironment(id)));
            });
        });
        specialForms.put(Symbol.get("quote").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands1(emitters, listValue, compileTimeOperand(), valueToQuote -> {
                if(asExpression)
                    emitters.add(instructions -> instructions.add(Instructions.loadConst(valueToQuote)));
            });
        });
        specialForms.put(Symbol.get("cons").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands2(emitters, listValue, runtimeOperand(), runtimeOperand(), (Object car, Object cdr) -> {
                if (asExpression)
                    emitters.add(instructions -> instructions.add(Instructions.cons()));
            });
        });
        specialForms.put(Symbol.get("car").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands1(emitters, listValue, runtimeOperand(), value -> {
                emitters.add(instructions -> instructions.add(Instructions.car()));
                if(!asExpression)
                    emitters.add(instructions -> instructions.add(Instructions.pop()));
            });
        });
        specialForms.put(Symbol.get("cdr").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands1(emitters, listValue, runtimeOperand(), value -> {
                emitters.add(instructions -> instructions.add(Instructions.cdr()));
                if(!asExpression)
                    emitters.add(instructions -> instructions.add(Instructions.pop()));
            });
        });
    }

    private interface OperandCompiler<T> {
        void compileTo(List<Consumer<List<Instruction>>> emitters, Object value);
    }

    private static <T> OperandCompiler<T> runtimeOperand() {
        return (emitters, value) -> {
            compileTo(value, emitters, true);
        };
    }

    private static <T> OperandCompiler<T> compileTimeOperand() {
        return (emitters, value) -> { };
    }

    private static <T0> void compileOperands1(List<Consumer<List<Instruction>>> emitters, ConsCell form,
                                                  OperandCompiler<T0> operand0Compiler,
                                                  Consumer<T0> formCompiler) {
        operand0Compiler.compileTo(emitters, form.getCdr().getCar());
        T0 operand0 = (T0) form.getCdr().getCar();
        formCompiler.accept(operand0);
    }

    private static <T0, T1> void compileOperands2(List<Consumer<List<Instruction>>> emitters, ConsCell form,
                                                  OperandCompiler<T0> operand0Compiler,
                                                  OperandCompiler<T1> operand1Compiler,
                                                  BiConsumer<T0, T1> formCompiler) {
        operand0Compiler.compileTo(emitters, form.getCdr().getCar());
        T0 operand0 = (T0) form.getCdr().getCar();
        operand1Compiler.compileTo(emitters, form.getCdr().getCdr().getCar());
        T1 operand1 = (T1) form.getCdr().getCdr().getCar();
        formCompiler.accept(operand0, operand1);
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
                SpecialForm specialForm = specialForms.get(symbolFirstValue.getCode());

                if(specialForm != null) {
                    specialForm.compileTo(emitters, listValue, asExpression);
                } else {
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
            } else {
                compileCons(listValue, emitters);
                if(!asExpression)
                    emitters.add(instructions -> instructions.add(Instructions.pop()));
            }
        }
    }

    private static void compileCons(ConsCell listValue, List<Consumer<List<Instruction>>> emitters) {
        compileTo(listValue.getCar(), emitters, true);
        if(listValue.getCdr() == null)
            emitters.add(instructions -> instructions.add(Instructions.loadConst(null)));
        else
            compileCons(listValue.getCdr(), emitters);
        emitters.add(instructions -> instructions.add(Instructions.cons()));
    }
}
