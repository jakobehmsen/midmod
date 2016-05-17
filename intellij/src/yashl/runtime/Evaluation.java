package yashl.runtime;

public class Evaluation {
    private Environment environment = new Environment();
    private Frame frame;
    private boolean running;

    public Evaluation(Frame frame) {
        this.frame = frame;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void evaluate() {
        running = true;
        while(running) {
            frame.evaluate(this);
        }
    }

    public static Object evaluate(Instruction[] instructions) {
        Evaluation evaluation = new Evaluation(new Frame(null, instructions));
        evaluation.evaluate();
        return evaluation.getFrame().pop();
    }

    private Object[] operands = new Object[0];

    public void popOperands(int count) {
        if(operands.length < count)
            operands = new Object[count];
        for(int i = 0; i < count; i++)
            operands[i] = getFrame().pop();
    }

    public Object getOperand(int index) {
        return operands[index];
    }

    public Object[] getOperands(int count) {
        Object[] operands = new Object[count];
        System.arraycopy(this.operands, 0, operands, 0, count);
        return operands;
    }

    public void halt() {
        running = false;
    }
}
