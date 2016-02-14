package midmod.pal.evaluation;

import midmod.rules.Environment;
import midmod.rules.RuleMap;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Frame {
    private Frame outer;
    private Instruction[] instructions;
    private int ip;
    private Stack<Object> locals = new Stack<>();

    private RuleMap signalHandler;
    private Frame signalFrame;

    public Frame(Instruction[] instructions) {
        this.instructions = instructions;
    }

    public Frame(Frame outer, Instruction[] instructions) {
        this.outer = outer;
        this.instructions = instructions;
    }

    public Frame(Frame outer, Instruction[] instructions, RuleMap signalHandler, Frame signalFrame) {
        this.outer = outer;
        this.instructions = instructions;
        this.signalHandler = signalHandler;
        this.signalFrame = signalFrame;
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

    public void forwardTo(Frame frame, int count) {
        for(int i = locals.size() - count; i < locals.size(); i++)
            frame.push(locals.get(i));
    }

    public void popForwardTo(Frame frame, int count) {
        forwardTo(frame, count);
        for(int i = 0; i < count; i++)
            this.pop();
    }

    public void signal(EvaluationContext ctx, int count, Object signal) {
        Environment captures = new Environment();
        List<Instruction> instructions = null;
        Frame targetFrame = this;
        Object signalComplex = Arrays.asList(this, signal);

        while (instructions == null) {
            if(targetFrame.signalHandler != null)
                instructions = signalHandler.resolveInstructions(signalComplex, captures, null);
            else {
                if(targetFrame.outer == null)
                    throw new CouldNotResolveSignalHandlerException(this, signal);
                targetFrame = targetFrame.outer;
            }
        }

        Frame signalHandlingFrame = new Frame(signalFrame, instructions.toArray(new Instruction[instructions.size()]));

        signalFrame.forwardTo(signalHandlingFrame, count);

        ctx.setFrame(signalHandlingFrame);
    }
}
