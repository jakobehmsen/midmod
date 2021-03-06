package yashl.runtime;

public class Instructions {
    public static Instruction loadConst(Object value) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().push(value);

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

    public static Instruction pop() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.getFrame().pop();

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction cons() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(2);
                Object car = evaluation.getOperand(1);
                ConsCell cdr = (ConsCell) evaluation.getOperand(0);
                evaluation.getFrame().push(new ConsCell(car, cdr));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction apply(int arity) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(arity + 2);
                Function function = (Function) evaluation.getOperand(arity);
                Environment environment = (Environment) evaluation.getOperand(arity + 1);
                function.apply(evaluation, environment, evaluation.getOperands(arity));
            }
        };
    }

    public static Instruction ret() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Frame outer = evaluation.getFrame().getOuter();
                outer.push(evaluation.getFrame().pop());
                evaluation.setFrame(outer);

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

    public static Instruction getEnvironment(Symbol symbol) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Environment environment = (Environment) evaluation.getFrame().pop();

                evaluation.getFrame().push(environment.get(symbol.getCode()));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction setEnvironment(Symbol symbol) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(2);
                Environment environment = (Environment) evaluation.getOperand(1);
                Object value = evaluation.getOperand(0);

                environment.set(symbol.getCode(), value);

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction car() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                ConsCell value = (ConsCell) evaluation.getFrame().pop();
                evaluation.getFrame().push(value.getCar());

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction cdr() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                ConsCell value = (ConsCell) evaluation.getFrame().pop();
                evaluation.getFrame().push(value.getCdr());

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction lambda() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(2);
                ConsCell params = (ConsCell) evaluation.getOperand(1);
                Object body = evaluation.getOperand(0);
                evaluation.getFrame().push(new Function(params, new ConsCell(body, null)));

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

    public static Instruction addi() {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(2);
                Number lhs = (Number)evaluation.getOperand(1);
                Number rhs = (Number)evaluation.getOperand(0);
                evaluation.getFrame().push(lhs.intValue() + rhs.intValue());

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
}
