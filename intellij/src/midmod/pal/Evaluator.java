package midmod.pal;

import midmod.pal.antlr4.PalBaseVisitor;
import midmod.pal.antlr4.PalLexer;
import midmod.pal.antlr4.PalParser;
import midmod.rules.RuleMap;
import midmod.rules.actions.*;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.Patterns;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

public class Evaluator {
    private RuleMap ruleMap;

    public Evaluator(RuleMap ruleMap) {
        this.ruleMap = ruleMap;
    }

    public Object evaluate(String sourceCode) throws IOException {
        return evaluate(new ByteArrayInputStream(sourceCode.getBytes()));
    }

    public Object evaluate(InputStream sourceCode) throws IOException {
        CharStream charStream = new ANTLRInputStream(sourceCode);
        PalLexer lexer = new PalLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PalParser parser = new PalParser(tokenStream);

        return evaluate(parser.script());
    }

    private Object evaluate(ParserRuleContext context) {
        return evaluateAction(context).perform(ruleMap, new Hashtable<>());

        /*
        // Should evaluateAction and then perform action

        return context.accept(new PalBaseVisitor<Object>() {
            @Override
            public Object visitScript(PalParser.ScriptContext ctx) {
                Object result = null;
                for (PalParser.ScriptElementContext elementContext : ctx.scriptElement()) {
                    result = evaluate(elementContext);
                }
                return result;
            }

            @Override
            public Object visitNumber(PalParser.NumberContext ctx) {
                return parseNumber(ctx);
            }

            @Override
            public Object visitString(PalParser.StringContext ctx) {
                return parseString(ctx);
            }

            @Override
            public Object visitList(PalParser.ListContext ctx) {
                return ctx.action().stream().map(x -> evaluate(x)).collect(Collectors.toList());
            }

            @Override
            public Object visitDefine(PalParser.DefineContext ctx) {
                Pattern pattern = evaluatePattern(ctx.pattern());
                Action action = evaluateAction(ctx.action());

                ruleMap.define(pattern, action);

                return action;
            }

            @Override
            public Object visitAction(PalParser.ActionContext ctx) {
                return ctx.accept(new PalBaseVisitor<Object>() {
                    @Override
                    public Object visitMaybeAction(PalParser.MaybeActionContext ctx) {
                        Object value = evaluate(ctx.actionTarget());

                        if(ctx.isCall != null)
                            return Call.on(ruleMap, value);

                        return value;
                    }

                    @Override
                    public Object visitAlwaysAction(PalParser.AlwaysActionContext ctx) {
                        Object value = ctx.action().stream().map(x -> evaluate(x)).collect(Collectors.toList());
                        return Call.on(ruleMap, value);
                    }
                });
            }
        });*/
    }

    private Action evaluateAction(ParserRuleContext ctx) {
        return ctx.accept(new PalBaseVisitor<Action>() {
            @Override
            public Action visitScript(PalParser.ScriptContext ctx) {
                List<Action> actions = ctx.scriptElement().stream().map(x -> evaluateAction(x)).collect(Collectors.toList());

                return new Block(actions);
            }

            @Override
            public Action visitExpression1(PalParser.Expression1Context ctx) {
                Action lhs = evaluateAction(ctx.expression2());

                for (PalParser.Expression1TailContext rhsCtx : ctx.expression1Tail()) {
                    Action rhs = evaluateAction(rhsCtx.expression1());
                    String operator = rhsCtx.BIN_OP1().getText();
                    lhs = new Call(listActionFromActions(Arrays.asList(new Constant(operator), lhs, rhs)));
                }

                return lhs;
            }

            @Override
            public Action visitExpression2(PalParser.Expression2Context ctx) {

                //ctx.isCall

                // Derive action
                // Conditionally wrap action into call
                Action actionTarget = evaluateAction(ctx.actionTarget());

                if(ctx.isCall != null)
                    return new Call(actionTarget);

                return actionTarget;
            }

            @Override
            public Action visitString(PalParser.StringContext ctx) {
                String str = parseString(ctx);
                return (ruleMap1, captures) -> str;
            }

            @Override
            public Action visitNumber(PalParser.NumberContext ctx) {
                Object number = parseNumber(ctx);
                return (ruleMap1, captures) -> number;
            }

            @Override
            public Action visitList(PalParser.ListContext ctx) {
                /*List<Action> actions = ctx.action().stream().map(x -> evaluateAction(x)).collect(Collectors.toList());
                return (ruleMap1, captures) ->
                    actions.stream().map(x -> {
                        Object y = x.perform(ruleMap1, captures);
                        if(y == null)
                            new String();
                        return y;
                    }).collect(Collectors.toList());*/

                return listActionFromContexts(ctx.action());
            }

            @Override
            public Action visitDefine(PalParser.DefineContext ctx) {
                Pattern pattern = evaluatePattern(ctx.pattern());
                Action action = evaluateAction(ctx.action());

                //ruleMap.define(pattern, action);

                return new Define(new Constant(pattern), new Constant(action));
            }

            @Override
            public Action visitAccess(PalParser.AccessContext ctx) {
                String name = ctx.getText();
                return (ruleMap1, captures) -> captures.get(name);
            }

            /*@Override
            public Action visitMaybeAction(PalParser.MaybeActionContext ctx) {
                Action target = ctx.actionTarget().accept(new PalBaseVisitor<Action>() {
                    @Override
                    public Action visitExpression1(PalParser.Expression1Context ctx) {
                        Action lhs = evaluateAction(ctx.expression2());

                        for (PalParser.Expression1Context rhsCtx : ctx.expression1()) {
                            Action rhs = evaluateAction(rhsCtx);
                            lhs = lhs.or(rhs);
                        }

                        return lhs;
                    }

                    @Override
                    public Action visitString(PalParser.StringContext ctx) {
                        String str = parseString(ctx);
                        return (ruleMap1, captures) -> str;
                    }

                    @Override
                    public Action visitNumber(PalParser.NumberContext ctx) {
                        Object number = parseNumber(ctx);
                        return (ruleMap1, captures) -> number;
                    }

                    @Override
                    public Action visitList(PalParser.ListContext ctx) {
                        List<Action> actions = ctx.action().stream().map(x -> evaluateAction(x)).collect(Collectors.toList());
                        return (ruleMap1, captures) ->
                            actions.stream().map(x -> x.perform(ruleMap1, captures)).collect(Collectors.toList());
                    }

                    @Override
                    public Action visitIdentifier(PalParser.IdentifierContext ctx) {
                        String name = ctx.getText();
                        return (ruleMap1, captures) -> captures.get(name);
                    }
                });

                return ctx.isCall != null ? new Call(target) : target;
            }*/

            @Override
            public Action visitAlwaysAction(PalParser.AlwaysActionContext ctx) {
                //List<Action> actions = ctx.action().stream().map(x -> evaluateAction(x)).collect(Collectors.toList());
                //Action action = (ruleMap1, captures) -> actions.stream().map(x -> x.perform(ruleMap1, captures)).collect(Collectors.toList());
                Action action = listActionFromContexts(ctx.action());
                return new Call(action);
            }
        });
    }

    private Action listActionFromContexts(List<PalParser.ActionContext> actionContexts) {
        List<Action> actions = actionContexts.stream().map(x -> evaluateAction(x)).collect(Collectors.toList());
        return listActionFromActions(actions);
    }

    private Action listActionFromActions(List<Action> actions) {
        return (ruleMap1, captures) ->
            actions.stream().map(x -> {
                Object y = x.perform(ruleMap1, captures);
                if(y == null)
                    return x.perform(ruleMap1, captures);
                return y;
            }).collect(Collectors.toList());
    }

    private Pattern evaluatePattern(PalParser.PatternContext ctx) {
        Pattern pattern = evaluatePatternTarget(ctx.pattern1());

        if (ctx.name != null)
            pattern = pattern.andThen(Patterns.capture(ctx.name.getText()));

        return pattern;
    }

    private Pattern evaluatePatternTarget(ParserRuleContext ctx) {
        return ctx.accept(new PalBaseVisitor<Pattern>() {
            @Override
            public Pattern visitPattern1(PalParser.Pattern1Context ctx) {
                Pattern lhs = evaluatePatternTarget(ctx.pattern2());

                for (PalParser.Pattern1Context rhsCtx : ctx.pattern1()) {
                    Pattern rhs = evaluatePatternTarget(rhsCtx);
                    lhs = lhs.or(rhs);
                }

                return lhs;
            }

            @Override
            public Pattern visitString(PalParser.StringContext ctx) {
                String str = parseString(ctx);
                return Patterns.equalsObject(str);
            }

            @Override
            public Pattern visitNumber(PalParser.NumberContext ctx) {
                Object number = parseNumber(ctx);
                if (number instanceof Integer)
                    return Patterns.equalsObject(number);
                else
                    return Patterns.equalsObject(number);
            }

            @Override
            public Pattern visitListPattern(PalParser.ListPatternContext ctx) {
                return Patterns.conformsTo(ctx.pattern().stream().map(x -> evaluatePattern(x)).collect(Collectors.toList()));
            }

            @Override
            public Pattern visitTypedPattern(PalParser.TypedPatternContext ctx) {
                Class<?> type = null;
                String typeName = ctx.type.getText();

                switch (typeName) {
                    case "Object":
                        type = Object.class;
                        break;
                    case "String":
                        type = String.class;
                        break;
                    case "Integer":
                        type = Integer.class;
                        break;
                    case "Double":
                        type = Double.class;
                        break;
                }

                return Patterns.is(type);
            }
        });
    }

    private String parseString(PalParser.StringContext ctx) {
        return ctx.getText().substring(1, ctx.getText().length() - 1);
    }

    private Object parseNumber(PalParser.NumberContext ctx) {
        try {
            return Integer.parseInt(ctx.getText());
        } catch (NumberFormatException e) {
            return Double.parseDouble(ctx.getText());
        }
    }
}
