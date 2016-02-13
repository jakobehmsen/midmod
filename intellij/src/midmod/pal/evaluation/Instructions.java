package midmod.pal.evaluation;

import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.actions.Action;
import midmod.rules.actions.Match;

import java.util.Arrays;
import java.util.List;

public class Instructions {
    public static Instruction ret = ctx -> {
        ctx.getFrame().getOuter().push(ctx.getFrame().pop());
        ctx.setFrame(ctx.getFrame().getOuter());
        ctx.getFrame().incIP();
    };

    public static Instruction constant(Object value) {
        return ctx -> {
            ctx.getFrame().push(value);
            ctx.getFrame().incIP();
        };
    }

    public static Instruction addi = ctx -> {
        int rhs = (int) ctx.getFrame().pop();
        int lhs = (int) ctx.getFrame().pop();
        ctx.getFrame().push(lhs + rhs);
        ctx.getFrame().incIP();
    };

    public static Instruction match(int count) {
        return ctx -> {
            // Stack expectations: [0..count, valueIn, rules] => [valueOut]
            Object valueIn = ctx.getFrame().pop();
            RuleMap rules = (RuleMap) ctx.getFrame().pop();

            Environment resolvedCaptures = new Environment();
            // localRules is passed as null because localRules is only used in Patterns.reference, which is obsolete
            // due to macros
            List<Instruction> instructions = rules.resolveInstructions(valueIn, resolvedCaptures, null);

            if (instructions == null)
                new String(); // Signal error

            Frame frame = new Frame(ctx.getFrame(), instructions.toArray(new Instruction[instructions.size()]));

            ctx.getFrame().forwardTo(frame, count);

            resolvedCaptures.getCaptured().forEach(x ->
                frame.push(x));

            ctx.setFrame(frame);
        };
    }

    public static Instruction stop = ctx -> ctx.stop();

    public static Instruction cons(int length) {
        return ctx -> {
            Object[] listContent = new Object[length];
            for(int i = length - 1; i >= 0; i--)
                listContent[i] = ctx.getFrame().pop();
            Object valueOut = Arrays.asList(listContent);
            ctx.getFrame().push(valueOut);
            ctx.getFrame().incIP();
        };
    }

    public static Instruction load(int index) {
        return ctx -> {
            ctx.getFrame().load(index);
            ctx.getFrame().incIP();
        };
    }
}
