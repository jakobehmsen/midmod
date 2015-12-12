package midmod.pal;

import midmod.pal.antlr4.PalBaseVisitor;
import midmod.pal.antlr4.PalLexer;
import midmod.pal.antlr4.PalParser;
import midmod.rules.RuleMap;
import midmod.rules.actions.Action;
import midmod.rules.actions.Call;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.Patterns;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
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
                Object value = evaluate(ctx.actionTarget());

                if(ctx.isCall != null)
                    return Call.on(ruleMap, value);

                return value;
            }
        });
    }

    private Action evaluateAction(PalParser.ActionContext ctx) {
        Action target = ctx.actionTarget().accept(new PalBaseVisitor<Action>() {
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
    }

    private Pattern evaluatePattern(PalParser.PatternContext ctx) {
        Pattern pattern = ctx.accept(new PalBaseVisitor<Pattern>() {
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
            public Pattern visitOpenPattern(PalParser.OpenPatternContext ctx) {
                Class<?> type = null;
                String typeName = ctx.type.getText();

                switch (typeName) {
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

                Pattern pattern = Patterns.is(type);

                if(ctx.name != null)
                    pattern = pattern.andThen(Patterns.capture(ctx.name.getText()));

                return pattern;
            }
        });

        return pattern;
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
