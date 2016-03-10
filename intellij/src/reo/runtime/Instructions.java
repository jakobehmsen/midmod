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
        };
    }
}
