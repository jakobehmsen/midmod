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
                Dictionary dict = (Dictionary) evaluation.getOperand(1);

                dict.put(name, value);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadSlot(String name) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Dictionary dict = (Dictionary) evaluation.getFrame().pop();

                evaluation.getFrame().push(dict.get(name));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction removeSlot(String name) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Dictionary dict = (Dictionary) evaluation.getFrame().pop();

                dict.remove(name);

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
}
