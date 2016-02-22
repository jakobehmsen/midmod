package jsflow.bindlang;

import jsflow.bindlang.antlr4.BindBaseVisitor;
import jsflow.bindlang.antlr4.BindLexer;
import jsflow.bindlang.antlr4.BindParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.stream.Collectors;

public class Parser {
    public static String replace(String text) {
        CharStream charStream = new ANTLRInputStream(text);
        BindLexer lexer = new BindLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        BindParser parser = new BindParser(tokenStream);

        return parse(parser.script());
    }

    private static String parse(ParserRuleContext parseCtx) {
        return parseCtx.accept(new BindBaseVisitor<String>() {
            @Override
            public String visitScript(BindParser.ScriptContext ctx) {
                return ctx.binding().stream().map(x ->
                    parse(x)).collect(Collectors.joining("\n"));
            }

            @Override
            public String visitBinding(BindParser.BindingContext ctx) {
                String targetExpressionStr =
                    "core.memberTarget(" + ctx.targetExpression.getText() + ", \"" + ctx.ID().getText() + "\")";
                String sourceExpressionStr = parseSource(ctx.sourceExpression);

                return "core.bind(" + sourceExpressionStr + ", " + targetExpressionStr + ")";
            }
        });
    }

    private static String parseSource(ParserRuleContext sourceCtx) {
        return sourceCtx.accept(new BindBaseVisitor<String>() {
            @Override
            public String visitExpression(BindParser.ExpressionContext ctx) {
                return parseSource(ctx.expression1());
            }

            @Override
            public String visitExpression1(BindParser.Expression1Context ctx) {
                String str = parseSource(ctx.expression2());

                for (BindParser.Expression1Context rhsCtx : ctx.expression1()) {
                    String lhsStr = str;
                    String rhsStr = parseSource(rhsCtx);
                    str = "core.reducer([" + lhsStr + ", " + rhsStr + "], function(x, y) {return x " + ctx.op.getText() + " y;})";
                }

                return str;
            }

            @Override
            public String visitExpression2(BindParser.Expression2Context ctx) {
                String str = parseSource(ctx.expression3());

                for (BindParser.Expression2Context rhsCtx : ctx.expression2()) {
                    String lhsStr = str;
                    String rhsStr = parseSource(rhsCtx);
                    str = "core.reducer([" + lhsStr + ", " + rhsStr + "], function(x, y) {return x " + ctx.op.getText() + " y;})";
                }

                return str;
            }

            @Override
            public String visitExpression3(BindParser.Expression3Context ctx) {
                String str = parseSource((ParserRuleContext) ctx.getChild(0));

                for (BindParser.ExpressionTailPartContext partCtx : ctx.expressionTail().expressionTailPart()) {
                    String strSource = str;
                    str = partCtx.accept(new BindBaseVisitor<String>() {
                        @Override
                        public String visitAccess(BindParser.AccessContext ctx) {
                            return "core.memberSource(" + strSource + ", \"" + ctx.ID().getText() + "\")";
                        }

                        @Override
                        public String visitCall(BindParser.CallContext ctx) {
                            return super.visitCall(ctx);
                        }
                    });
                }

                return str;
            }

            @Override
            public String visitString(BindParser.StringContext ctx) {
                return "core.constSource(" + ctx.getText() + ")";
            }

            @Override
            public String visitNumber(BindParser.NumberContext ctx) {
                return "core.constSource(" + ctx.getText() + ")";
            }

            @Override
            public String visitAccess(BindParser.AccessContext ctx) {
                return ctx.getText();
            }
        });
    }
}
