package reo.runtime;

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
                ((AbstractRObject)target).put(selector.getValue(), value);
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
                evaluation.getFrame().push(new CustomRObject(prototype));
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
}
