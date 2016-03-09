package reo.runtime;

import java.util.Stack;

public class Frame {
    private Frame outer;
    private Instruction[] instructions;
    private int ip;
    private Stack<RObject> stack = new Stack<>();

    public Frame(Frame outer, Instruction[] instructions) {
        this.outer = outer;
        this.instructions = instructions;
    }

    public RObject peek() {
        return stack.peek();
    }

    public void evaluate(Evaluation evaluation) {
        instructions[ip].evaluate(evaluation);
    }

    public Frame getOuter() {
        return outer;
    }

    public void push(RObject value) {
        stack.push(value);
    }

    public RObject pop() {
        return stack.pop();
    }

    public void pop(RObject[] array, int count) {
        for(int i = 0; i < count; i++)
            array[i] = stack.pop();
    }

    public void push(int ordinal) {
        stack.push(stack.get(ordinal));
    }

    public void push(RObject[] array) {
        for(int i = 0; i < array.length; i++)
            push(array[i]);
    }

    public void incrementIP() {
        ip++;
    }
}
