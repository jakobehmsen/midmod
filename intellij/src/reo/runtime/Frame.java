package reo.runtime;

import java.util.Stack;

public class Frame {
    private Frame outer;
    private Instruction[] instructions;
    private int ip;
    private Stack<Observable> stack = new Stack<>();

    public Frame(Frame outer, Instruction[] instructions) {
        this.outer = outer;
        this.instructions = instructions;
    }

    public void evaluate(Evaluation evaluation) {
        instructions[ip].evaluate(evaluation);
    }

    public Observable pop() {
        return stack.pop();
    }

    public void push(Observable value) {
        stack.push(value);
    }

    public void incrementIP() {
        ip++;
    }

    public void load(int ordinal) {
        stack.push(stack.get(0));
    }

    public void allocate(int count) {
        for(int i = 0; i < count; i++)
            stack.push(null);
    }

    public void set(Observable[] values, int offset) {
        for(int i = 0; i < values.length; i++)
            stack.set(offset + i, values[i]);
    }

    public void set(int ordinal, Observable value) {
        stack.set(ordinal, value);
    }

    public Frame getOuter() {
        return outer;
    }

    public void dup() {
        stack.push(stack.peek());
    }
}
