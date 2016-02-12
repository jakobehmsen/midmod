package midmod.pal.evaluation;

import java.util.Stack;

public class Frame {
    private Frame outer;
    private Instruction[] instructions;
    private int ip;
    private Stack<Object> locals = new Stack<>();

    public Frame(Instruction[] instructions) {
        this.instructions = instructions;
    }

    public Frame(Frame outer, Instruction[] instructions) {
        this.outer = outer;
        this.instructions = instructions;
    }

    public void evaluateInstruction(EvaluationContext ctx) {
        instructions[ip].evaluate(ctx);
    }

    public void push(Object value) {
        locals.push(value);
    }

    public Object pop() {
        return locals.pop();
    }

    public Object get(int index) {
        return locals.get(index);
    }

    public void incIP() {
        ip++;
    }

    public void load(int index) {
        locals.push(locals.get(index));
    }

    public Frame getOuter() {
        return outer;
    }
}
