package reo.runtime;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Instructions {
    public static Instruction ret() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Frame frame = evaluation.getFrame();
                RObject result = frame.peek();
                Frame outerFrame = frame.getOuter();
                outerFrame.push(result);
                evaluation.setFrame(outerFrame);
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction send(String selector, int arity) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RObject[] arguments = new RObject[arity];
                evaluation.getFrame().pop(arguments, arity);
                RObject receiver = evaluation.getFrame().pop();
                receiver.send(evaluation, selector, arguments);
            }
        };
    }

    public static Instruction loadIntegerPrototype() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(evaluation.getUniverse().getIntegerPrototype());
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadDoublePrototype() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(evaluation.getUniverse().getDoublePrototype());
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadFunctionPrototype() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(evaluation.getUniverse().getFunctionPrototype());
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadStringPrototype() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(evaluation.getUniverse().getStringPrototype());
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadArrayPrototype() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(evaluation.getUniverse().getArrayPrototype());
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadInstructionPrototype() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RString instructionName = (RString) evaluation.getFrame().pop();
                evaluation.getFrame().push(evaluation.getUniverse().getInstructionPrototype(instructionName.getValue()));
                evaluation.getFrame().incrementIP();
            }
        };
    }

    @FirstClassValues
    public static Instruction loadConst(RObject value) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(value);
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadLocal(int ordinal) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(ordinal);
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction halt() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.halt();
            }
        };
    }

    public static Instruction addi() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                IntegerRObject rhs = (IntegerRObject) evaluation.getFrame().pop();
                IntegerRObject lhs = (IntegerRObject) evaluation.getFrame().pop();
                evaluation.getFrame().push(new IntegerRObject(lhs.getValue() + rhs.getValue()));
                evaluation.getFrame().incrementIP();
            }

            @Override
            public boolean isFunctional() {
                return true;
            }
        };
    }

    public static Instruction loadNull() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(evaluation.getUniverse().getNull());
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction storeLocal(int ordinal) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RObject value = evaluation.getFrame().pop();
                evaluation.getFrame().set(ordinal, value);
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction dup() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().dup();
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction dup2() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().dup2();
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction loadSlot() {
        return new Instruction() {
            @Override
            public boolean isFunctional() {
                return true;
            }

            @Override
            public void evaluate(Evaluation evaluation) {
                RString selector = (RString) evaluation.getFrame().pop();
                RObject target = evaluation.getFrame().pop();
                evaluation.getFrame().push(target.resolve(evaluation, selector.getValue()));
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction storeSlot() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RObject value = evaluation.getFrame().pop();
                RString selector = (RString) evaluation.getFrame().pop();
                RObject target = evaluation.getFrame().pop();
                ((DeltaRObject)target).put(selector.getValue(), value);
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction pop() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().pop();
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction newa() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                IntegerRObject length = (IntegerRObject) evaluation.getFrame().pop();
                RObject[] array = new RObject[(int)length.getValue()];
                evaluation.getFrame().pop(array, (int)length.getValue());
                evaluation.getFrame().push(new RArray(array));
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction newf() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RArray instructions = (RArray) evaluation.getFrame().pop();
                Instruction[] nativeInstructions = new Instruction[instructions.getValue().length];
                for(int i = 0; i < instructions.getValue().length; i++)
                    nativeInstructions[i] = ((InstructionRObject)instructions.getValue()[i]).getValue();
                evaluation.getFrame().push(new FunctionRObject(new Behavior(nativeInstructions)));
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction applyf() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RArray arguments = (RArray) evaluation.getFrame().pop();
                RObject receiver = evaluation.getFrame().pop();
                FunctionRObject function = (FunctionRObject) evaluation.getFrame().pop();
                function.apply(evaluation, receiver, arguments.getValue());
            }
        };
    }

    public static Instruction geta() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                IntegerRObject index = (IntegerRObject) evaluation.getFrame().pop();
                RArray array = (RArray) evaluation.getFrame().pop();
                evaluation.getFrame().push(array.getValue()[(int)index.getValue()]);
                evaluation.getFrame().incrementIP();
            }

            @Override
            public boolean isFunctional() {
                return true;
            }
        };
    }

    public static Instruction seta() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RObject value = evaluation.getFrame().pop();
                IntegerRObject index = (IntegerRObject) evaluation.getFrame().pop();
                RArray array = (RArray) evaluation.getFrame().pop();
                array.getValue()[(int)index.getValue()] = value;
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction newo() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RObject prototype = evaluation.getFrame().pop();
                evaluation.getFrame().push(new DeltaRObject(prototype));
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction swap() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().swap();
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction swap1() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().swap1();
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction setPrototype() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                RObject prototype = evaluation.getFrame().pop();
                RObject target = evaluation.getFrame().pop();
                ((DeltaRObject)target).setPrototype(prototype);
                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction newInstruction() {
        return new Instruction() {
            @Override
            public boolean isFunctional() {
                return true;
            }

            @Override
            public void evaluate(Evaluation evaluation) {
                RArray creationOperands = (RArray) evaluation.getFrame().pop();
                RString instructionName = (RString) evaluation.getFrame().pop();

                Object[] arguments = new Object[creationOperands.getValue().length];
                boolean useNativeValues = evaluation.getUniverse().instructionUsesNativeValues(instructionName.getValue());
                for(int i = 0; i < creationOperands.getValue().length; i++) {
                    Object argument = useNativeValues ? creationOperands.getValue()[i].toNative() : creationOperands.getValue()[i];
                    arguments[i] = argument;
                }

                InstructionRObject instruction = evaluation.getUniverse().getInstruction(instructionName.getValue(), arguments);
                evaluation.getFrame().push(instruction);

                evaluation.getFrame().incrementIP();
            }
        };
    }
}
