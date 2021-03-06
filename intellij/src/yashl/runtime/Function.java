package yashl.runtime;

import yashl.function.QuadConsumer;
import yashl.function.TriConsumer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Function {
    private ConsCell parameters;
    private Object value;
    private Map<Symbol, Integer> parametersAsSymbols;
    private Instruction[] instructions;

    public Function(Object value) {
        parameters = null;
        this.value = value;
    }

    public Function(ConsCell parameters, Object value) {
        this.parameters = parameters;
        this.value = value;
    }

    public void apply(Evaluation evaluation, Environment environment, Object[] arguments) {
        ensureCompiled();
        Frame callFrame = new Frame(evaluation.getFrame(), instructions);
        callFrame.push(environment);
        for(int i = 0; i < arguments.length; i++)
            callFrame.push(arguments[i]);
        evaluation.setFrame(callFrame);
    }

    private void ensureCompiled() {
        if(instructions == null) {
            parametersAsSymbols = new Hashtable<>();
            parametersAsSymbols.put(Symbol.get("this"), 0);
            ConsCell currentParam = parameters;
            while(currentParam != null) {
                parametersAsSymbols.put((Symbol) currentParam.getCar(), parametersAsSymbols.size());
                currentParam = currentParam.getCdr();
            }
            instructions = compile(parametersAsSymbols, value, instructions -> instructions.add(Instructions.ret()));
        }
    }

    private interface Emitter {
        void accept(Map<Symbol, Integer> localNameToOrdinal, List<Instruction> instructions);
    }

    public static Instruction[] compile(Map<Symbol, Integer> localNameToOrdinal, Object value, Consumer<List<Instruction>> postEmitter) {
        ArrayList<Emitter> emitters = new ArrayList<>();

        if(!(value instanceof ConsCell)) {
            compileTo(value, emitters, true);
        } else {
            ConsCell currentCell = (ConsCell) value;
            while (currentCell != null) {
                boolean asExpression = currentCell.getCdr() == null;
                compileTo(currentCell.getCar(), emitters, asExpression);
                currentCell = currentCell.getCdr();
            }
        }
        ArrayList<Instruction> instructions = new ArrayList<>();
        emitters.forEach(e -> e.accept(localNameToOrdinal, instructions));
        postEmitter.accept(instructions);
        return instructions.toArray(new Instruction[instructions.size()]);
    }

    private interface SpecialForm {
        void compileTo(List<Emitter> emitters, ConsCell form, boolean asExpression);
    }

    private static Hashtable<Integer, SpecialForm> specialForms = new Hashtable<>();

    static {
        specialForms.put(Symbol.get("set").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands3(emitters, listValue, runtimeOperand(), compileTimeOperand(), runtimeOperand(), (Object environment, Symbol id, Object valueForId) -> {
                if(asExpression)
                    emitters.add((l, instructions) -> instructions.add(Instructions.dup2()));
                emitters.add((l, instructions) -> instructions.add(Instructions.setEnvironment(id)));
            });
        });
        specialForms.put(Symbol.get("get").getCode(), (emitters, listValue, asExpression) -> {
            if(asExpression) {
                compileOperands2(emitters, listValue, runtimeOperand(), compileTimeOperand(), (Object environment, Symbol id) -> {
                    emitters.add((l, instructions) -> instructions.add(Instructions.getEnvironment(id)));
                });
            }
        });
        specialForms.put(Symbol.get("let").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands2(emitters, listValue, compileTimeOperand(), runtimeOperand(), (Symbol id, Object valueForId) -> {
                // Allocate local id
                // Extract allocated index
                //locals.add(id.getName());
                //int ordinal = locals.size();
                if(asExpression)
                    emitters.add((l, instructions) -> instructions.add(Instructions.dup()));
                //emitters.add(instructions -> instructions.add(Instructions.store(ordinal)));
            });
        });
        specialForms.put(Symbol.get("quote").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands1(emitters, listValue, compileTimeOperand(), valueToQuote -> {
                if(asExpression)
                    emitters.add((l, instructions) -> instructions.add(Instructions.loadConst(valueToQuote)));
            });
        });
        specialForms.put(Symbol.get("cons").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands2(emitters, listValue, runtimeOperand(), runtimeOperand(), (Object car, Object cdr) -> {
                if (asExpression)
                    emitters.add((l, instructions) -> instructions.add(Instructions.cons()));
            });
        });
        specialForms.put(Symbol.get("car").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands1(emitters, listValue, runtimeOperand(), value -> {
                emitters.add((l, instructions) -> instructions.add(Instructions.car()));
                if(!asExpression)
                    emitters.add((l, instructions) -> instructions.add(Instructions.pop()));
            });
        });
        specialForms.put(Symbol.get("cdr").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands1(emitters, listValue, runtimeOperand(), value -> {
                emitters.add((l, instructions) -> instructions.add(Instructions.cdr()));
                if(!asExpression)
                    emitters.add((l, instructions) -> instructions.add(Instructions.pop()));
            });
        });
        specialForms.put(Symbol.get("lambda").getCode(), (emitters, listValue, asExpression) -> {
            compileOperands2(emitters, listValue, compileTimeOperand(), compileTimeOperand(), (Object params, Object body) -> {
                if (asExpression) {
                    emitters.add((l, instructions) -> instructions.add(Instructions.loadConst(params)));
                    emitters.add((l, instructions) -> instructions.add(Instructions.loadConst(body)));
                    emitters.add((l, instructions) -> instructions.add(Instructions.lambda()));
                }
            });
        });
        specialForms.put(Symbol.get("apply").getCode(), (emitters, listValue, asExpression) -> {
            compileTo(listValue.getCdr().getCar(), emitters, true);
            compileTo(listValue.getCdr().getCdr().getCar(), emitters, true);

            int arityTmp = 0;
            ConsCell argCell = listValue.getCdr().getCdr().getCdr();
            while(argCell != null) {
                compileTo(argCell.getCar(), emitters, true);
                argCell = argCell.getCdr();
                arityTmp++;
            }

            int arity = arityTmp;

            emitters.add((l, instructions) -> instructions.add(Instructions.apply(arity)));

            if (!asExpression)
                emitters.add((l, instructions) -> instructions.add(Instructions.pop()));
        });
        specialForms.put(Symbol.get("addi").getCode(), (emitters, listValue, asExpression) -> {
            if (asExpression) {
                compileOperands2(emitters, listValue, runtimeOperand(), runtimeOperand(), (Object lhs, Object rhs) -> {
                    emitters.add((l, instructions) -> instructions.add(Instructions.addi()));
                });
            }
        });
    }

    private interface OperandCompiler<T> {
        void compileTo(List<Emitter> emitters, Object value);
    }

    private static <T> OperandCompiler<T> runtimeOperand() {
        return (emitters, value) -> {
            compileTo(value, emitters, true);
        };
    }

    private static <T> OperandCompiler<T> compileTimeOperand() {
        return (emitters, value) -> { };
    }

    private static <T0> void compileOperands1(List<Emitter> emitters, ConsCell form,
                                              OperandCompiler<T0> operand0Compiler,
                                              Consumer<T0> formCompiler) {
        operand0Compiler.compileTo(emitters, form.getCdr().getCar());
        T0 operand0 = (T0) form.getCdr().getCar();
        formCompiler.accept(operand0);
    }

    private static <T0, T1> void compileOperands2(List<Emitter> emitters, ConsCell form,
                                                  OperandCompiler<T0> operand0Compiler,
                                                  OperandCompiler<T1> operand1Compiler,
                                                  BiConsumer<T0, T1> formCompiler) {
        operand0Compiler.compileTo(emitters, form.getCdr().getCar());
        T0 operand0 = (T0) form.getCdr().getCar();
        operand1Compiler.compileTo(emitters, form.getCdr().getCdr().getCar());
        T1 operand1 = (T1) form.getCdr().getCdr().getCar();
        formCompiler.accept(operand0, operand1);
    }

    private static <T0, T1, T2> void compileOperands3(List<Emitter> emitters, ConsCell form,
                                                  OperandCompiler<T0> operand0Compiler,
                                                  OperandCompiler<T1> operand1Compiler,
                                                  OperandCompiler<T2> operand2Compiler,
                                                  TriConsumer<T0, T1, T2> formCompiler) {
        operand0Compiler.compileTo(emitters, form.getCdr().getCar());
        T0 operand0 = (T0) form.getCdr().getCar();
        operand1Compiler.compileTo(emitters, form.getCdr().getCdr().getCar());
        T1 operand1 = (T1) form.getCdr().getCdr().getCar();
        operand2Compiler.compileTo(emitters, form.getCdr().getCdr().getCdr().getCar());
        T2 operand2 = (T2) form.getCdr().getCdr().getCdr().getCar();
        formCompiler.accept(operand0, operand1, operand2);
    }

    public static void compileTo(Object value, List<Emitter> emitters, boolean asExpression) {
        if(!(value instanceof ConsCell)) {
            if(asExpression) {
                if (value instanceof Symbol) {
                    Symbol symbolValue = (Symbol) value;
                    emitters.add((locals, instructions) -> {
                        Integer ordinal = locals.get(symbolValue);

                        if(ordinal != null)
                            instructions.add(Instructions.load(ordinal));
                        else
                            instructions.add(Instructions.getEnvironment(symbolValue));
                    });
                } else
                    emitters.add((locals, instructions) -> instructions.add(Instructions.loadConst(value)));
            }
        } else {
            ConsCell listValue = (ConsCell) value;

            Object firstValue = listValue.getCar();

            SpecialForm specialForm = null;

            if(firstValue instanceof Symbol) {
                Symbol symbolFirstValue = (Symbol) firstValue;
                specialForm = specialForms.get(symbolFirstValue.getCode());
            }

            if(specialForm != null) {
                specialForm.compileTo(emitters, listValue, asExpression);
            } else {
                // Forward environment stored at 0
                emitters.add((locals, instructions) -> instructions.add(Instructions.load(0)));

                compileTo(firstValue, emitters, true);

                int arityTmp = 0;
                ConsCell currentCell = listValue.getCdr();
                while(currentCell != null) {
                    compileTo(currentCell.getCar(), emitters, true);
                    currentCell = currentCell.getCdr();
                    arityTmp++;
                }
                int arity = arityTmp;

                emitters.add((locals, instructions) -> instructions.add(Instructions.apply(arity)));
                if(!asExpression)
                    emitters.add((locals, instructions) -> instructions.add(Instructions.pop()));
            }
        }
    }
}
