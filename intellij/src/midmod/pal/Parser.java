package midmod.pal;

import midmod.pal.antlr4.PalBaseVisitor;
import midmod.pal.antlr4.PalLexer;
import midmod.pal.antlr4.PalParser;
import midmod.rules.Environment;
import midmod.rules.actions.*;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.PatternFactory;
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

public class Parser {
    public Object parse(String sourceCode) throws IOException {
        return parse(new ByteArrayInputStream(sourceCode.getBytes()));
    }

    public Object parse(InputStream sourceCode) throws IOException {
        CharStream charStream = new ANTLRInputStream(sourceCode);
        PalLexer lexer = new PalLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PalParser parser = new PalParser(tokenStream);

        return parse(parser.script());
    }

    private Object parse(ParserRuleContext context) {
        Object actionValue = parseAction(context, new MetaEnvironment(null));
        return actionValue;
        //return ActionFactory.perform(actionValue, ActionFactory.globalRules(), ActionFactory.localRules(), ActionFactory.list());
    }

    private List<Object> parseAction(ParserRuleContext ctx, MetaEnvironment nameToCaptureAddressMap) {
        return ctx.accept(new PalBaseVisitor<List<Object>>() {
            @Override
            public List<Object> visitScript(PalParser.ScriptContext ctx) {
                List<Object> actionValues = ctx.scriptElement().stream().map(x -> parseAction(x, nameToCaptureAddressMap)).collect(Collectors.toList());

                return ActionFactory.block(actionValues);
            }

            @Override
            public List<Object> visitExpression1(PalParser.Expression1Context ctx) {
                List<Object> lhsActionValue = parseAction(ctx.expression2(), nameToCaptureAddressMap);

                for (PalParser.Expression1TailContext rhsCtx : ctx.expression1Tail()) {
                    List<Object> rhsActionValue = parseAction(rhsCtx.expression1(), nameToCaptureAddressMap);
                    String operator = rhsCtx.BIN_OP1().getText();
                    //lhsActionValue = ActionFactory.match(ActionFactory.list(ActionFactory.constant(operator), lhsActionValue, rhsActionValue));
                    lhsActionValue = ActionFactory.list(ActionFactory.constant(operator), lhsActionValue, rhsActionValue);
                }

                return lhsActionValue;
            }

            @Override
            public List<Object> visitExpression2(PalParser.Expression2Context ctx) {
                List<Object> lhsActionValue = parseAction(ctx.expression3(), nameToCaptureAddressMap);

                for (PalParser.Expression2TailContext rhsCtx : ctx.expression2Tail()) {
                    List<Object> rhsActionValue = parseAction(rhsCtx.expression1(), nameToCaptureAddressMap);
                    String operator = rhsCtx.BIN_OP2().getText();
                    //lhsActionValue = ActionFactory.match(ActionFactory.list(ActionFactory.constant(operator), lhsActionValue, rhsActionValue));
                    lhsActionValue = ActionFactory.list(ActionFactory.constant(operator), lhsActionValue, rhsActionValue);
                }

                return lhsActionValue;
            }

            @Override
            public List<Object> visitString(PalParser.StringContext ctx) {
                String str = parseString(ctx);
                return ActionFactory.constant(str);
            }

            @Override
            public List<Object> visitExpression3(PalParser.Expression3Context ctx) {
                List<Object> actionTargetValue = parseAction(ctx.actionTarget(), nameToCaptureAddressMap);

                if(ctx.isCall != null)
                    return ActionFactory.match(actionTargetValue);


                return actionTargetValue;
            }

            @Override
            public List<Object> visitNumber(PalParser.NumberContext ctx) {
                Object number = parseNumber(ctx);
                return ActionFactory.constant(number);
            }

            @Override
            public List<Object> visitList(PalParser.ListContext ctx) {
                return ActionFactory.list(ctx.action().stream().map(x -> parseAction(x, nameToCaptureAddressMap)).collect(Collectors.toList()));
            }

            // TODO: Add support for the remaining types of actions and patterns

            /*@Override
            public Action visitMap(PalParser.MapContext ctx) {
                if(ctx.isMap != null) {
                    List<Map.Entry<String, Action>> slots = ctx.slot().stream()
                        .map(x -> new AbstractMap.SimpleImmutableEntry<>(x.ID().getText(), evaluateAction(x.action(), nameToCaptureAddressMap))).collect(Collectors.toList());

                    return new NewMap(slots);
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
                }
            }*/

            @Override
            public List<Object> visitDefineNameAndParams(PalParser.DefineNameAndParamsContext ctx) {
                MetaEnvironment nameToCaptureAddressMapForDef = new MetaEnvironment(nameToCaptureAddressMap);
                String name = ctx.ID().getText();
                //Environment environment = new Environment();
                List<Object> paramPatterns = ctx.pattern().stream()
                    .map(x -> parsePattern(x, Arrays.asList(0), nameToCaptureAddressMapForDef))
                    .collect(Collectors.toList());
                ArrayList<Object> patterns = new ArrayList<>();
                patterns.add(Patterns.equalsObject(name));
                patterns.addAll(paramPatterns);
                List<Object> pattern = PatternFactory.subsumesList(patterns);
                List<Object> action = parseAction(ctx.action(), nameToCaptureAddressMapForDef);

                //return new Define(new Constant(pattern), new Constant(action));

                return ActionFactory.define(ActionFactory.globalRules(), pattern, action);
            }

            @Override
            public List<Object> visitDefine(PalParser.DefineContext ctx) {
                MetaEnvironment nameToCaptureAddressMapForDef = new MetaEnvironment(nameToCaptureAddressMap);
                List<Object> patternValue = parsePattern(ctx.pattern(), Arrays.asList(0), nameToCaptureAddressMapForDef);
                List<Object> actionValue = parseAction(ctx.action(), nameToCaptureAddressMapForDef);

                if(ctx.name != null) {
                    String name = ctx.name.getText();
                    return ActionFactory.define(
                        ActionFactory.globalRules(),
                        ActionFactory.constant(PatternFactory.equalsObject(name)),
                        ActionFactory.constant(actionValue)
                    );
                    //return new Define(new Constant(Patterns.equalsObject(name)), new Constant(new Constant(new Rule(pattern, action))));
                }

                return ActionFactory.define(
                    ActionFactory.globalRules(),
                    ActionFactory.constant(patternValue),
                    ActionFactory.constant(actionValue)
                );
                //return new Define(new Constant(pattern), new Constant(action));
            }

            /*@Override
            public Action visitAccess(PalParser.AccessContext ctx) {
                String name = ctx.getText();
                return nameToCaptureAddressMap.createActionFor(name);
            }*/

            @Override
            public List<Object> visitNameAndArgs(PalParser.NameAndArgsContext ctx) {
                ArrayList<Object> actions = new ArrayList<>();
                String name = ctx.name.getText();
                actions.add(ActionFactory.constant(name));
                actions.addAll(ctx.action().stream().map(x -> parseAction(x, nameToCaptureAddressMap)).collect(Collectors.toList()));
                List<Object> action = ActionFactory.list(actions);
                return ActionFactory.match(action);
                //Action action = listActionFromActions(actions);
                //return new Match(action);
            }

            /*@Override
            public Action visitPatternLiteral(PalParser.PatternLiteralContext ctx) {
                List<Integer> captureAddress = new ArrayList<>();
                //MetaEnvironment patternLiteralNameToCaptureAddressMap = new MetaEnvironment(null);
                Function<Environment, Pattern> patternSupplier = evaluatePattern(ctx.pattern(), captureAddress, nameToCaptureAddressMap);
                return (ruleMap1, local, captures) ->
                    patternSupplier.apply(captures);
            }*/
        });
    }

    private List<Object> parsePattern(PalParser.PatternContext ctx, List<Integer> captureAddress, MetaEnvironment nameToCaptureAddressMap) {
        /*if(ctx.isAction != null) {
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
        }*/

        List<Object> patternTargetValue = parsePatternTarget(ctx.pattern1(), captureAddress, nameToCaptureAddressMap);
        boolean isRepeat = false;

        if(ctx.repeatPattern != null) {
            List<Object> repeatPatternTargetValue = patternTargetValue;
            patternTargetValue = PatternFactory.repeat(repeatPatternTargetValue);
            //patternTargetConstructorValue = captures -> Patterns.repeat(repeatPatternConstructorTargetValue.apply(captures));
            isRepeat = true;
        }

        if (ctx.name != null) {
            // Declare index for parameter
            int index = nameToCaptureAddressMap.size();
            nameToCaptureAddressMap.put(ctx.name.getText(), index);
            List<Object> capturePatternTargetValue = patternTargetValue;
            patternTargetValue = !isRepeat
                ? PatternFactory.captureSingle(index, capturePatternTargetValue)
                : PatternFactory.captureMany(index, capturePatternTargetValue);
        }

        return patternTargetValue;
    }

    private List<Object> parsePatternTarget(ParserRuleContext ctx, List<Integer> captureAddress, MetaEnvironment nameToCaptureAddressMap) {
        return ctx.accept(new PalBaseVisitor<List<Object>>() {
            @Override
            public List<Object> visitPattern1(PalParser.Pattern1Context ctx) {
                List<Object> lhs = parsePatternTarget(ctx.pattern2(), captureAddress, nameToCaptureAddressMap);

                for (PalParser.Pattern1Context rhsCtx : ctx.pattern1()) {
                    List<Object> rhs = parsePatternTarget(rhsCtx, captureAddress, nameToCaptureAddressMap);
                    List<Object> theLhs = lhs;
                    lhs = PatternFactory.or(theLhs, rhs);
                    //lhs = captures -> theLhs.apply(captures).or(rhs.apply(captures));
                }

                return lhs;
            }

            @Override
            public List<Object> visitString(PalParser.StringContext ctx) {
                String str = parseString(ctx);
                return PatternFactory.equalsObject(str);
            }

            @Override
            public List<Object> visitNumber(PalParser.NumberContext ctx) {
                Object number = parseNumber(ctx);
                return PatternFactory.equalsObject(number);
            }

            @Override
            public List<Object> visitListPattern(PalParser.ListPatternContext ctx) {
                List<Object> patterns = IntStream.range(0, ctx.pattern().size()).mapToObj(i -> {
                    ArrayList<Integer> newCaptureAddress = new ArrayList<>(captureAddress);
                    newCaptureAddress.add(i);
                    return parsePattern(ctx.pattern().get(i), newCaptureAddress, nameToCaptureAddressMap);
                }).collect(Collectors.toList());
                return PatternFactory.subsumesList(patterns);
                //return captures -> Patterns.subsumesList(patterns.stream().map(x -> x.apply(captures)).collect(Collectors.toList()));
            }

            @Override
            public List<Object> visitMapPattern(PalParser.MapPatternContext ctx) {
                if(ctx.isMap != null) {
                    List<Object> slots = ctx.slotPattern().stream()
                        .map(x -> PatternFactory.slotDefinition(x.ID().getText(), parsePattern(x.pattern(), captureAddress, nameToCaptureAddressMap))).collect(Collectors.toList());

                    return PatternFactory.subsumesMap(slots);
                    //return captures -> Patterns.subsumesToMap(slots.stream().map(x ->
                    //    new AbstractMap.SimpleImmutableEntry<>(x.getKey(), x.getValue().apply(captures))).collect(Collectors.toList()));
                } else {
                    List<Object> patterns = ctx.pattern().stream()
                        .map(x -> parsePattern(x, captureAddress, nameToCaptureAddressMap)).collect(Collectors.toList());

                    return PatternFactory.subsumesRuleMap(patterns);
                    //return captures -> Patterns.subsumesToRuleMap(patterns.stream().map(x -> x.apply(captures)).collect(Collectors.toList()));
                }
            }

            @Override
            public List<Object> visitReferencePattern(PalParser.ReferencePatternContext ctx) {
                String name = ctx.name.getText();

                throw new UnsupportedOperationException("visitReferencePattern");
                //return captures -> Patterns.reference(ruleMap, name);
            }

            @Override
            public List<Object> visitTypedPattern(PalParser.TypedPatternContext ctx) {
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

                return PatternFactory.is(theType);

                //return captures -> Patterns.is(theType);
            }

            @Override
            public List<Object> visitNotPattern(PalParser.NotPatternContext ctx) {
                List<Object> patternToNegate = parsePatternTarget(ctx.pattern1(), captureAddress, nameToCaptureAddressMap);

                return PatternFactory.not(patternToNegate);
                //return captures -> Patterns.not(patternToNegate.apply(captures));
            }

            @Override
            public List<Object> visitAnything(PalParser.AnythingContext ctx) {
                return PatternFactory.anything();
                //return captures -> Patterns.anything;
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
