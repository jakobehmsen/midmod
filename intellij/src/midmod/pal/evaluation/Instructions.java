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

    public static Instruction globalRules = ctx -> {
        ctx.getFrame().push(ctx.getFrame().get(0));
        ctx.getFrame().incIP();
    };

    public static Instruction localRules = ctx -> {
        ctx.getFrame().push(ctx.getFrame().get(1));
        ctx.getFrame().incIP();
    };

    public static Instruction match = ctx -> {
        Object valueIn = ctx.getFrame().pop();
        RuleMap localRules = (RuleMap)ctx.getFrame().pop();
        RuleMap globalRules = (RuleMap)ctx.getFrame().pop();

        Environment resolvedCaptures = new Environment();
        List<Instruction> instructions = globalRules.resolveInstructions(valueIn, resolvedCaptures, localRules);

        if(instructions == null)
            new String(); // Signal error

        ctx.setFrame(new Frame(ctx.getFrame(), instructions.toArray(new Instruction[instructions.size()])));
        resolvedCaptures.getCaptured().forEach(x ->
            ctx.getFrame().push(x));
    };

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
