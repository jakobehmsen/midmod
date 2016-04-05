package reo_OLD.runtime;

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
        for(int i = count - 1; i >= 0; i--)
            array[i] = stack.pop();
    }

    public void push(int ordinal) {
        stack.push(stack.get(ordinal));
    }

    public void push(RObject[] array) {
        //for(int i = array.length - 1; i >= 0; i--)
        for(int i = 0; i < array.length; i++)
            push(array[i]);
    }

    public void incrementIP() {
        ip++;
    }

    public void set(int ordinal, RObject value) {
        stack.set(ordinal, value);
    }

    public void dup() {
        stack.push(stack.peek());
    }

    public void dup2() {
        stack.add(stack.size() - 3, stack.peek());
    }

    public void swap() {
        RObject tmp = stack.peek();
        stack.set(stack.size() - 1, stack.get(stack.size() - 2));
        stack.set(stack.size() - 2, tmp);
    }

    public void swap1() {
        RObject tmp = stack.peek();
        stack.set(stack.size() - 1, stack.get(stack.size() - 3));
        stack.set(stack.size() - 3, tmp);
    }
}
