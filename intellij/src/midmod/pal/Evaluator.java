package midmod.pal;

import midmod.pal.antlr4.PalBaseVisitor;
import midmod.pal.antlr4.PalLexer;
import midmod.pal.antlr4.PalParser;
import midmod.rules.Environment;
import midmod.rules.Rule;
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
import java.util.function.Function;
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

    public Object evaluateAsValue(InputStream sourceCode) throws IOException {
        CharStream charStream = new ANTLRInputStream(sourceCode);
        PalLexer lexer = new PalLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PalParser parser = new PalParser(tokenStream);

        return evaluateAsValue(parser.script());
    }

    private Object evaluateAsValue(ParserRuleContext context) {
        Action action = evaluateAction(context, new MetaEnvironment(null));
        Object actionAsValue = action.toValue();
        actionAsValue = expandMacros(actionAsValue);
        action = parseActionFromValue(actionAsValue);
        return action.perform(ruleMap, ruleMap, new Environment());
    }

    private RuleMap actionParser = new RuleMap();

    {
        Constant.defineParseRule(actionParser);
        Block.defineParseRule(actionParser);
    }

    private Action parseActionFromValue(Object value) {
        return (Action)Match.on(actionParser, actionParser, value);
    }

    private Object expandMacros(Object value) {
        return value;
    }

    public Object evaluate(InputStream sourceCode) throws IOException {
        CharStream charStream = new ANTLRInputStream(sourceCode);
        PalLexer lexer = new PalLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PalParser parser = new PalParser(tokenStream);

        return evaluate(parser.script());
    }

    private Object evaluate(ParserRuleContext context) {
        return evaluateAction(context, new MetaEnvironment(null)).perform(ruleMap, ruleMap, new Environment());
    }

    private Action evaluateAction(ParserRuleContext ctx, MetaEnvironment nameToCaptureAddressMap) {
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
                    lhs = new Match(listActionFromActions(Arrays.asList(new Constant(operator), lhs, rhs)));
                }

                return lhs;
            }

            @Override
            public Action visitExpression2(PalParser.Expression2Context ctx) {
                Action lhs = evaluateAction(ctx.expression3(), nameToCaptureAddressMap);

                for (PalParser.Expression2TailContext rhsCtx : ctx.expression2Tail()) {
                    Action rhs = evaluateAction(rhsCtx.expression1(), nameToCaptureAddressMap);
                    String operator = rhsCtx.BIN_OP2().getText();
                    lhs = new Match(listActionFromActions(Arrays.asList(new Constant(operator), lhs, rhs)));
                }

                return lhs;
            }

            @Override
            public Action visitExpression3(PalParser.Expression3Context ctx) {
                Action actionTarget = evaluateAction(ctx.actionTarget(), nameToCaptureAddressMap);

                if(ctx.isCall != null)
                    return new Match(actionTarget);

                return actionTarget;
            }

            @Override
            public Action visitString(PalParser.StringContext ctx) {
                String str = parseString(ctx);
                return new Constant(str);
                //return (ruleMap1, captures) -> str;
            }

            @Override
            public Action visitNumber(PalParser.NumberContext ctx) {
                Object number = parseNumber(ctx);
                return new Constant(number);
                //return (ruleMap1, captures) -> number;
            }

            @Override
            public Action visitList(PalParser.ListContext ctx) {
                return listActionFromContexts(ctx.action(), nameToCaptureAddressMap);
            }

            @Override
            public Action visitMap(PalParser.MapContext ctx) {
                if(ctx.isMap != null) {
                    List<Map.Entry<String, Action>> slots = ctx.slot().stream()
                        .map(x -> new AbstractMap.SimpleImmutableEntry<>(x.ID().getText(), evaluateAction(x.action(), nameToCaptureAddressMap))).collect(Collectors.toList());

                    return new NewMap(slots);

                    /*return new Action() {
                        @Override
                        public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
                            return slots.stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().perform(ruleMap, local, captures)));
                        }

                        @Override
                        public Object toValue() {
                            return Arrays.asList("map", slots.stream().map(x -> Arrays.asList(x.getKey(), x.getValue().toValue())).collect(Collectors.toList()));
                        }
                    };*/
                } else {
                    // Should be a closure around the lexical context.
                    // I.e., is shouldn't be a constant but instead be created dynamically (where needed/captured are used)
                    //RuleMap ruleMap = new RuleMap();

                    MetaEnvironment nameToCaptureAddressMapForDef = new MetaEnvironment(nameToCaptureAddressMap);

                    Map<Pattern, Action> patternActionMap = ctx.define().stream().map(x -> {
                        Pattern pattern = evaluatePattern(x.pattern(), Arrays.asList(0), nameToCaptureAddressMapForDef).apply(new Environment());
                        Action action = evaluateAction(x.action(), nameToCaptureAddressMapForDef);
                        return new AbstractMap.SimpleImmutableEntry<>(pattern, new Constant(action));
                    }).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

                    nameToCaptureAddressMapForDef.addClosedCaptures(patternActionMap);

                    return new NewRuleMap(patternActionMap);

                    /*return new Action() {
                        @Override
                        public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
                            RuleMap newRuleMap = new RuleMap();

                            nameToCaptureAddressMapForDef.setupRuleMap(ruleMap, newRuleMap, captures);

                            patternActionMap.entrySet().forEach(x -> newRuleMap.define(x.getKey(), x.getValue()));

                            return newRuleMap;
                        }
                    };*/

                    //return new Constant(ruleMap);
                }
            }

            @Override
            public Action visitDefineNameAndParams(PalParser.DefineNameAndParamsContext ctx) {
                MetaEnvironment nameToCaptureAddressMapForDef = new MetaEnvironment(nameToCaptureAddressMap);
                String name = ctx.ID().getText();
                Environment environment = new Environment();
                List<Pattern> paramPatterns = ctx.pattern().stream()
                    .map(x -> evaluatePattern(x, Arrays.asList(0), nameToCaptureAddressMapForDef))
                    .map(x -> x.apply(environment))
                    .collect(Collectors.toList());
                ArrayList<Pattern> patterns = new ArrayList<>();
                patterns.add(Patterns.equalsObject(name));
                patterns.addAll(paramPatterns);
                Pattern pattern = Patterns.conformsTo(patterns);
                Action action = evaluateAction(ctx.action(), nameToCaptureAddressMapForDef);

                return new Define(new Constant(pattern), new Constant(action));
            }

            @Override
            public Action visitDefine(PalParser.DefineContext ctx) {
                MetaEnvironment nameToCaptureAddressMapForDef = new MetaEnvironment(nameToCaptureAddressMap);
                Pattern pattern = evaluatePattern(ctx.pattern(), Arrays.asList(0), nameToCaptureAddressMapForDef).apply(new Environment());
                Action action = evaluateAction(ctx.action(), nameToCaptureAddressMapForDef);

                if(ctx.name != null) {
                    String name = ctx.name.getText();
                    return new Define(new Constant(Patterns.equalsObject(name)), new Constant(new Constant(new Rule(pattern, action))));
                }

                return new Define(new Constant(pattern), new Constant(action));
            }

            @Override
            public Action visitAccess(PalParser.AccessContext ctx) {
                String name = ctx.getText();
                return nameToCaptureAddressMap.createActionFor(name);
                /*int index = nameToCaptureAddressMap.getIndexFor(name);

                return new Action() {
                    @Override
                    public Object perform(RuleMap ruleMap, Environment captures) {
                        Object val = captures.get(index);
                        return val;
                    }

                    @Override
                    public Object toValue() {
                        return Arrays.asList("access", index);
                    }
                };*/

                /*return (ruleMap1, captures) -> {
                    Object val = captures.getIndexFor(index);
                    return val;
                };*/
            }

            @Override
            public Action visitNameAndArgs(PalParser.NameAndArgsContext ctx) {
                ArrayList<Action> actions = new ArrayList<>();
                String name = ctx.name.getText();
                actions.add(new Constant(name));
                actions.addAll(ctx.action().stream().map(x -> evaluateAction(x, nameToCaptureAddressMap)).collect(Collectors.toList()));
                Action action = listActionFromActions(actions);
                return new Match(action);
            }

            @Override
            public Action visitPatternLiteral(PalParser.PatternLiteralContext ctx) {
                List<Integer> captureAddress = new ArrayList<>();
                //MetaEnvironment patternLiteralNameToCaptureAddressMap = new MetaEnvironment(null);
                Function<Environment, Pattern> patternSupplier = evaluatePattern(ctx.pattern(), captureAddress, nameToCaptureAddressMap);
                return (ruleMap1, local, captures) ->
                    patternSupplier.apply(captures);
            }
        });
    }

    private Action listActionFromContexts(List<PalParser.ActionContext> actionContexts, MetaEnvironment nameToCaptureAddressMap) {
        List<Action> actions = actionContexts.stream().map(x -> evaluateAction(x, nameToCaptureAddressMap)).collect(Collectors.toList());
        return listActionFromActions(actions);
    }

    private Action listActionFromActions(List<Action> actions) {
        return new Action() {
            @Override
            public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
                return actions.stream().map(x -> {
                    Object y = x.perform(ruleMap, local, captures);
                    if(y == null)
                        return x.perform(ruleMap, local, captures);
                    return y;
                }).collect(Collectors.toList());
            }

            @Override
            public Object toValue() {
                return Arrays.asList("list", actions.stream().map(x -> x.toValue()).collect(Collectors.toList()));
            }
        };

        /*return (ruleMap1, captures) ->
            actions.stream().map(x -> {
                Object y = x.perform(ruleMap1, captures);
                if(y == null)
                    return x.perform(ruleMap1, captures);
                return y;
            }).collect(Collectors.toList());*/
    }

    /*private Action evaluatePatternFromAction(PalParser.PatternContext ctx, List<Integer> captureAddress, MetaEnvironment nameToCaptureAddressMap) {
        if(ctx.isAction != null) {
            //return Patterns.action();
            // What should the outer environment be?
            //MetaEnvironment metaNameToCaptureAddressMap = new MetaEnvironment(null);
            Action action = evaluateAction(ctx.mesaAction().action(), nameToCaptureAddressMap);
            // Should some non-pattern values implicitly be converted into patterns? Such as string and ints?
            return (ruleMap1, local1, captures) -> {
                Pattern pattern;
                Object res = action.perform(ruleMap, ruleMap, captures);
                if(res instanceof Pattern)
                    pattern = (Pattern)res;
                else
                    pattern = Patterns.equalsObject(res);
                return pattern;
            };
        }

        Action patternTarget = evaluatePatternTarget(ctx.pattern1(), captureAddress, nameToCaptureAddressMap);
        boolean isRepeat = false;

        if(ctx.repeatPattern != null) {
            Action repeatPatternTarget = patternTarget;
            patternTarget = captures -> Patterns.repeat(repeatPatternTarget.apply(captures));
            isRepeat = true;
        }

        if (ctx.name != null) {
            // Declare index for parameter
            int index = nameToCaptureAddressMap.size();
            nameToCaptureAddressMap.put(ctx.name.getText(), index);
            Action capturePatternTarget = patternTarget;
            patternTarget = !isRepeat
                ? captures -> Patterns.captureSingle(index, capturePatternTarget.apply(captures))
                : captures -> Patterns.captureMany(index, capturePatternTarget.apply(captures));
        }

        return patternTarget;
    }*/

    private Function<Environment, Pattern> evaluatePattern(PalParser.PatternContext ctx, List<Integer> captureAddress, MetaEnvironment nameToCaptureAddressMap) {
        if(ctx.isAction != null) {
            //return Patterns.action();
            // What should the outer environment be?
            //MetaEnvironment metaNameToCaptureAddressMap = new MetaEnvironment(null);
            Action action = evaluateAction(ctx.mesaAction().action(), nameToCaptureAddressMap);
            // Should some non-pattern values implicitly be converted into patterns? Such as string and ints?
            return captures -> {
                Pattern pattern;
                Object res = action.perform(ruleMap, ruleMap, captures);
                if(res instanceof Pattern)
                    pattern = (Pattern)res;
                else
                    pattern = Patterns.equalsObject(res);
                return pattern;
            };
        }

        Function<Environment, Pattern> patternTarget = evaluatePatternTarget(ctx.pattern1(), captureAddress, nameToCaptureAddressMap);
        boolean isRepeat = false;

        if(ctx.repeatPattern != null) {
            Function<Environment, Pattern> repeatPatternTarget = patternTarget;
            patternTarget = captures -> Patterns.repeat(repeatPatternTarget.apply(captures));
            isRepeat = true;
        }

        if (ctx.name != null) {
            // Declare index for parameter
            int index = nameToCaptureAddressMap.size();
            nameToCaptureAddressMap.put(ctx.name.getText(), index);
            Function<Environment, Pattern> capturePatternTarget = patternTarget;
            patternTarget = !isRepeat
                ? captures -> Patterns.captureSingle(index, capturePatternTarget.apply(captures))
                : captures -> Patterns.captureMany(index, capturePatternTarget.apply(captures));
        }

        return patternTarget;
    }

    private Function<Environment, Pattern> evaluatePatternTarget(ParserRuleContext ctx, List<Integer> captureAddress, MetaEnvironment nameToCaptureAddressMap) {
        return ctx.accept(new PalBaseVisitor<Function<Environment, Pattern>>() {
            @Override
            public Function<Environment, Pattern> visitPattern1(PalParser.Pattern1Context ctx) {
                Function<Environment, Pattern> lhs = evaluatePatternTarget(ctx.pattern2(), captureAddress, nameToCaptureAddressMap);

                for (PalParser.Pattern1Context rhsCtx : ctx.pattern1()) {
                    Function<Environment, Pattern> rhs = evaluatePatternTarget(rhsCtx, captureAddress, nameToCaptureAddressMap);
                    Function<Environment, Pattern> theLhs = lhs;
                    lhs = captures -> theLhs.apply(captures).or(rhs.apply(captures));
                }

                return lhs;
            }

            @Override
            public Function<Environment, Pattern> visitString(PalParser.StringContext ctx) {
                String str = parseString(ctx);
                return captures -> Patterns.equalsObject(str);
            }

            @Override
            public Function<Environment, Pattern> visitNumber(PalParser.NumberContext ctx) {
                Object number = parseNumber(ctx);
                if (number instanceof Integer)
                    return captures -> Patterns.equalsObject(number);
                else
                    return captures -> Patterns.equalsObject(number);
            }

            @Override
            public Function<Environment, Pattern> visitListPattern(PalParser.ListPatternContext ctx) {
                List<Function<Environment, Pattern>> patterns = IntStream.range(0, ctx.pattern().size()).mapToObj(i -> {
                    ArrayList<Integer> newCaptureAddress = new ArrayList<Integer>(captureAddress);
                    newCaptureAddress.add(i);
                    return evaluatePattern(ctx.pattern().get(i), newCaptureAddress, nameToCaptureAddressMap);
                }).collect(Collectors.toList());
                return captures -> Patterns.conformsTo(patterns.stream().map(x -> x.apply(captures)).collect(Collectors.toList()));
            }

            @Override
            public Function<Environment, Pattern> visitMapPattern(PalParser.MapPatternContext ctx) {
                if(ctx.isMap != null) {
                    List<Map.Entry<String, Function<Environment, Pattern>>> slots = ctx.slotPattern().stream()
                        .map(x -> new AbstractMap.SimpleImmutableEntry<>(x.ID().getText(), evaluatePattern(x.pattern(), captureAddress, nameToCaptureAddressMap))).collect(Collectors.toList());

                    return captures -> Patterns.subsumesToMap(slots.stream().map(x ->
                        new AbstractMap.SimpleImmutableEntry<>(x.getKey(), x.getValue().apply(captures))).collect(Collectors.toList()));
                } else {
                    List<Function<Environment, Pattern>> patterns = ctx.pattern().stream()
                        .map(x -> evaluatePattern(x, captureAddress, nameToCaptureAddressMap)).collect(Collectors.toList());

                    return captures -> Patterns.subsumesToRuleMap(patterns.stream().map(x -> x.apply(captures)).collect(Collectors.toList()));
                }
            }

            @Override
            public Function<Environment, Pattern> visitReferencePattern(PalParser.ReferencePatternContext ctx) {
                String name = ctx.name.getText();

                return captures -> Patterns.reference(ruleMap, name);
            }

            @Override
            public Function<Environment, Pattern> visitTypedPattern(PalParser.TypedPatternContext ctx) {
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

                Class<?> theType = type;

                return captures -> Patterns.is(theType);
            }

            @Override
            public Function<Environment, Pattern> visitNotPattern(PalParser.NotPatternContext ctx) {
                Function<Environment, Pattern> patternToNegate = evaluatePatternTarget(ctx.pattern1(), captureAddress, nameToCaptureAddressMap);

                return captures -> Patterns.not(patternToNegate.apply(captures));
            }

            @Override
            public Function<Environment, Pattern> visitAnything(PalParser.AnythingContext ctx) {
                return captures -> Patterns.anything;
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
