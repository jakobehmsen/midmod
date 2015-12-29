package midmod.pal;

import midmod.pal.antlr4.PalBaseVisitor;
import midmod.pal.antlr4.PalLexer;
import midmod.pal.antlr4.PalParser;
import midmod.rules.Environment;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        return evaluateAction(context, new Hashtable<>()).perform(ruleMap, new Environment());

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

    private Action evaluateAction(ParserRuleContext ctx, Map<String, List<Integer>> nameToCaptureAddressMap) {
        return ctx.accept(new PalBaseVisitor<Action>() {
            @Override
            public Action visitScript(PalParser.ScriptContext ctx) {
                List<Action> actions = ctx.scriptElement().stream().map(x -> evaluateAction(x, nameToCaptureAddressMap)).collect(Collectors.toList());

                return new Block(actions);
            }

            @Override
            public Action visitExpression1(PalParser.Expression1Context ctx) {
                Action lhs = evaluateAction(ctx.expression2(), nameToCaptureAddressMap);

                for (PalParser.Expression1TailContext rhsCtx : ctx.expression1Tail()) {
                    Action rhs = evaluateAction(rhsCtx.expression1(), nameToCaptureAddressMap);
                    String operator = rhsCtx.BIN_OP1().getText();
                    lhs = new Call(listActionFromActions(Arrays.asList(new Constant(operator), lhs, rhs)));
                }

                return lhs;
            }

            @Override
            public Action visitExpression2(PalParser.Expression2Context ctx) {
                Action lhs = evaluateAction(ctx.expression3(), nameToCaptureAddressMap);

                for (PalParser.Expression2TailContext rhsCtx : ctx.expression2Tail()) {
                    Action rhs = evaluateAction(rhsCtx.expression1(), nameToCaptureAddressMap);
                    String operator = rhsCtx.BIN_OP2().getText();
                    lhs = new Call(listActionFromActions(Arrays.asList(new Constant(operator), lhs, rhs)));
                }

                return lhs;
            }

            @Override
            public Action visitExpression3(PalParser.Expression3Context ctx) {

                //ctx.isCall

                // Derive action
                // Conditionally wrap action into call
                Action actionTarget = evaluateAction(ctx.actionTarget(), nameToCaptureAddressMap);

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

                return listActionFromContexts(ctx.action(), nameToCaptureAddressMap);
            }

            @Override
            public Action visitMap(PalParser.MapContext ctx) {
                List<Map.Entry<String, Action>> slots = ctx.slot().stream()
                    .map(x -> new AbstractMap.SimpleImmutableEntry<>(x.ID().getText(), evaluateAction(x.action(), nameToCaptureAddressMap))).collect(Collectors.toList());

                return (ruleMap1, captures) ->
                    slots.stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().perform(ruleMap1, captures)));
            }

            @Override
            public Action visitDefine(PalParser.DefineContext ctx) {
                Pattern pattern = evaluatePattern(ctx.pattern(), Arrays.asList(0), nameToCaptureAddressMap);
                Action action = evaluateAction(ctx.action(), nameToCaptureAddressMap);

                //ruleMap.define(pattern, action);

                return new Define(new Constant(pattern), new Constant(action));
            }

            @Override
            public Action visitAccess(PalParser.AccessContext ctx) {
                String name = ctx.getText();
                List<Integer> captureAddress = nameToCaptureAddressMap.get(name);
                // Resolve address
                //return (ruleMap1, captures) -> captures.get(name);
                //return null;

                return (ruleMap1, captures) -> {
                    Object val = captures.get(captureAddress.get(0));
                    for(int i = 1; i < captureAddress.size(); i++)
                        val = ((List<Object>)val).get(captureAddress.get(i));
                    return val;
                };
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
                Action action = listActionFromContexts(ctx.action(), nameToCaptureAddressMap);
                return new Call(action);
            }
        });
    }

    private Action listActionFromContexts(List<PalParser.ActionContext> actionContexts, Map<String, List<Integer>> nameToCaptureAddressMap) {
        /*List<Action> actions = IntStream.range(0, actionContexts.size()).mapToObj(i -> {
            ArrayList<Object> newCaptureAddress = new ArrayList<Object>(nameToCaptureAddressMap);
            newCaptureAddress.add(i);
            return evaluateAction(actionContexts.get(i), nameToCaptureAddressMap, captureAddress);
        }).collect(Collectors.toList());*/

        List<Action> actions = actionContexts.stream().map(x -> evaluateAction(x, nameToCaptureAddressMap)).collect(Collectors.toList());
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

    private Pattern evaluatePattern(PalParser.PatternContext ctx, List<Integer> captureAddress, Map<String, List<Integer>> nameToCaptureAddressMap) {
        Pattern pattern = evaluatePatternTarget(ctx.pattern1(), captureAddress, nameToCaptureAddressMap);
        boolean isRepeat = false;

        if(ctx.repeatPattern != null) {
            pattern = Patterns.repeat(pattern);
            isRepeat = true;
        }

        if (ctx.name != null)
            nameToCaptureAddressMap.put(ctx.name.getText(), captureAddress);
            // Resolve address in captures/environment
            pattern = pattern;
            //pattern = Patterns.capture(pattern, ctx.name.getText(), !isRepeat);
            //pattern = pattern.andThen(Patterns.capture(ctx.name.getText()));

        // TODO:
        // Put consumption right after last part of pattern, to fix issues like
        // or'ing where consumption other may occur multiple times
        // pattern = pattern.andThen(Patterns.consume);
        // Probably not the right thing to do: Instead, backtrack within or, something like:
        // 0: mark
        // 1: first case.match
        // 2: rewind
        // 3: second case match

        return pattern;
    }

    private Pattern evaluatePatternTarget(ParserRuleContext ctx, List<Integer> captureAddress, Map<String, List<Integer>> nameToCaptureAddressMap) {
        return ctx.accept(new PalBaseVisitor<Pattern>() {
            @Override
            public Pattern visitPattern1(PalParser.Pattern1Context ctx) {
                Pattern lhs = evaluatePatternTarget(ctx.pattern2(), captureAddress, nameToCaptureAddressMap);

                for (PalParser.Pattern1Context rhsCtx : ctx.pattern1()) {
                    Pattern rhs = evaluatePatternTarget(rhsCtx, captureAddress, nameToCaptureAddressMap);
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
                return Patterns.conformsTo(IntStream.range(0, ctx.pattern().size()).mapToObj(i -> {
                    ArrayList<Integer> newCaptureAddress = new ArrayList<Integer>(captureAddress);
                    newCaptureAddress.add(i);
                    return evaluatePattern(ctx.pattern().get(i), newCaptureAddress, nameToCaptureAddressMap);
                }).collect(Collectors.toList()));

                /*return Patterns.conformsTo(ctx.pattern().stream().map(x -> {
                    return evaluatePattern(x, captureAddress, nameToCaptureAddressMap);
                }).collect(Collectors.toList()));*/
            }

            @Override
            public Pattern visitMapPattern(PalParser.MapPatternContext ctx) {
                List<Map.Entry<String, Pattern>> slots = ctx.slotPattern().stream()
                    .map(x -> new AbstractMap.SimpleImmutableEntry<>(x.ID().getText(), evaluatePattern(x.pattern(), captureAddress, nameToCaptureAddressMap))).collect(Collectors.toList());

                return Patterns.conformsToMap(slots);
            }

            @Override
            public Pattern visitTypedPattern(PalParser.TypedPatternContext ctx) {
                Class<?> type = null;
                String typeName = ctx.type.getText();

                switch (typeName.charAt(1)) {
                    case 's':
                        type = String.class;
                        break;
                    case 'i':
                        type = Integer.class;
                        break;
                    case 'd':
                        type = Double.class;
                        break;
                }

                return Patterns.is(type);
            }

            @Override
            public Pattern visitAnything(PalParser.AnythingContext ctx) {
                return Patterns.anything;
            }
        });
    }

    private String parseString(PalParser.StringContext ctx) {
        String rawString = ctx.getText().substring(1, ctx.getText().length() - 1);
        return rawString.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }

    private Object parseNumber(PalParser.NumberContext ctx) {
        try {
            return Integer.parseInt(ctx.getText());
        } catch (NumberFormatException e) {
            return Double.parseDouble(ctx.getText());
        }
    }
}
