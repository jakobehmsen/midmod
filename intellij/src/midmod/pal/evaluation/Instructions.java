package midmod.pal.evaluation;

import midmod.rules.RuleMap;
import midmod.rules.actions.Match;

public class Instructions {
    public static Instruction constant(Object value) {
        return ctx -> {
            ctx.getFrame().push(value);
            ctx.getFrame().incIP();
        };
    }

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
        Object valueOut = Match.on(globalRules, localRules, valueIn);
        ctx.getFrame().push(valueOut);
        ctx.getFrame().incIP();
    };

    public static Instruction stop = ctx -> ctx.stop();
}
