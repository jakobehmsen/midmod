package midmod.pal.evaluation;

import midmod.rules.RuleMap;

public class Evaluator {
    private Frame frame;

    public Object evaluate(RuleMap globalRules, RuleMap localRules, Instruction[] instructions) {
        frame = new Frame(instructions);
        frame.push(globalRules);
        frame.push(localRules);
        return evaluate();
    }

    private Object evaluate() {
        EvaluationContext ctx = new EvaluationContext(frame);

        while(ctx.isRunning())
            frame.evaluateInstruction(ctx);

        return frame.pop();
    }
}
