package yashl.runtime;

import java.util.Stack;

public class Frame {
    private Frame outer;
    private Instruction[] instructions;
    private int ip;
    private Stack<Object> stack = new Stack<>();

    public Frame(Frame outer, Instruction[] instructions) {
        this.outer = outer;
        this.instructions = instructions;
    }

    public void evaluate(Evaluation evaluation) {
        instructions[ip].evaluate(evaluation);
    }

    public void push(Object value) {
        stack.push(value);
    }

    public Object pop() {
        return stack.pop();
    }

    public void incrementIP() {
        ip++;
    }

    public Frame getOuter() {
        return outer;
    }

    public void dup() {
        stack.push(stack.peek());
    }

    public void dup2() {
        stack.add(stack.size() - 2, stack.peek());
    }

    public void load(int ordinal) {
        stack.push(stack.get(ordinal));
    }
}
