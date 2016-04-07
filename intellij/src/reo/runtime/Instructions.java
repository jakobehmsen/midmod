package reo.runtime;

import java.util.Arrays;
import java.util.function.Function;

public class Instructions {
    public static Instruction halt() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.halt();
            }
        };
    }

    public static Instruction loadConstant(Object value) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(new Constant(value));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction load(int ordinal) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().load(ordinal);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction storeSlot(String name) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(2);

                Observable value = evaluation.getOperand(0);
                Observable dictObs = evaluation.getOperand(1);

                Observables.setSlot(dictObs, name, value);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadSlot(String name) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Observable dictObs = evaluation.getFrame().pop();

                evaluation.getFrame().push(Observables.getSlot(dictObs, name));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction removeSlot(String name) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Observable dictObs = evaluation.getFrame().pop();

                Observables.removeSlot(dictObs, name);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction messageSend(String selector, int arity) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(arity);
                Observable[] arguments = evaluation.getOperands();
                Observable receiverObs = evaluation.getFrame().pop();

                evaluation.getFrame().push(Observables.messageSend(evaluation.getUniverse(), receiverObs, selector, arguments));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction addi() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(2);

                Observable lhs = evaluation.getOperand(0);
                Observable rhs = evaluation.getOperand(1);

                evaluation.getFrame().push(new Reducer(Arrays.asList(lhs, rhs), new Function<Object[], Object>() {
                    @Override
                    public Object apply(Object[] objects) {
                        return (int)objects[0] + (int)objects[1];
                    }

                    @Override
                    public String toString() {
                        return "addi";
                    }
                }));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction newDict() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(new Constant(new Dictionary()));

                evaluation.getFrame().incrementIP();
            }
        };
    }
}
