package midmod.pal.evaluation;

import java.util.Stack;

public class Frame {
    private Instruction[] instructions;
    private int ip;
    private Stack<Object> locals = new Stack<>();

    public Frame(Instruction[] instructions) {
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
}
