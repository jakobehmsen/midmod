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
                Object car = evaluation.getOperand(0);
                ConsCell cdr = (ConsCell) evaluation.getOperand(1);
                evaluation.getFrame().push(new ConsCell(car, cdr));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction call(Symbol symbol, int arity) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                evaluation.popOperands(arity);
                Function function = (Function) evaluation.getEnvironment().get(symbol.getCode());
                function.apply(evaluation, evaluation.getOperands());

                evaluation.getFrame().incrementIP();
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
                evaluation.getFrame().push(evaluation.getEnvironment().get(symbol.getCode()));

                evaluation.getFrame().incrementIP();
            }
        };
    }

    public static Instruction setEnvironment(Symbol symbol) {
        return new Instruction() {
            @Override
            public void evaluate(Evaluation evaluation) {
                Object value = evaluation.getFrame().pop();
                evaluation.getEnvironment().set(symbol.getCode(), value);

                evaluation.getFrame().incrementIP();
            }
        };
    }
}
