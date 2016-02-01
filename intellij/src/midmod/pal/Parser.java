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
import org.antlr.v4.codegen.ParserFactory;
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
                    lhsActionValue = ActionFactory.match(ActionFactory.list(ActionFactory.constant(operator), lhsActionValue, rhsActionValue));
                }

                return lhsActionValue;
            }

            @Override
            public List<Object> visitExpression2(PalParser.Expression2Context ctx) {
                List<Object> lhsActionValue = parseAction(ctx.expression3(), nameToCaptureAddressMap);

                for (PalParser.Expression2TailContext rhsCtx : ctx.expression2Tail()) {
                    List<Object> rhsActionValue = parseAction(rhsCtx.expression1(), nameToCaptureAddressMap);
                    String operator = rhsCtx.BIN_OP2().getText();
                    lhsActionValue = ActionFactory.match(ActionFactory.list(ActionFactory.constant(operator), lhsActionValue, rhsActionValue));
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

            /*@Override
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
                Pattern pattern = Patterns.subsumesList(patterns);
                Action action = evaluateAction(ctx.action(), nameToCaptureAddressMapForDef);

                return new Define(new Constant(pattern), new Constant(action));
            }*/

            /*@Override
            public Action visitDefine(PalParser.DefineContext ctx) {
                MetaEnvironment nameToCaptureAddressMapForDef = new MetaEnvironment(nameToCaptureAddressMap);
                Pattern pattern = evaluatePattern(ctx.pattern(), Arrays.asList(0), nameToCaptureAddressMapForDef).apply(new Environment());
                Action action = evaluateAction(ctx.action(), nameToCaptureAddressMapForDef);

                if(ctx.name != null) {
                    String name = ctx.name.getText();
                    return new Define(new Constant(Patterns.equalsObject(name)), new Constant(new Constant(new Rule(pattern, action))));
                }

                return new Define(new Constant(pattern), new Constant(action));
            }*/

            /*@Override
            public Action visitAccess(PalParser.AccessContext ctx) {
                String name = ctx.getText();
                return nameToCaptureAddressMap.createActionFor(name);
            }*/

            /*@Override
            public Action visitNameAndArgs(PalParser.NameAndArgsContext ctx) {
                ArrayList<Action> actions = new ArrayList<>();
                String name = ctx.name.getText();
                actions.add(new Constant(name));
                actions.addAll(ctx.action().stream().map(x -> evaluateAction(x, nameToCaptureAddressMap)).collect(Collectors.toList()));
                Action action = listActionFromActions(actions);
                return new Match(action);
            }*/

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
