package reo.lang;

import jsflow.bindlang.antlr4.ReoBaseVisitor;
import jsflow.bindlang.antlr4.ReoLexer;
import jsflow.bindlang.antlr4.ReoParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import reo.runtime.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Parser {
    public static Statement parse(String text, boolean implicitReturn) {
        CharStream charStream = new ANTLRInputStream(text);
        ReoLexer lexer = new ReoLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReoParser parser = new ReoParser(tokenStream);

        return parseBlock(parser.block(), implicitReturn);
    }

    private static Statement parseBlock(ReoParser.BlockContext ctx, boolean implicitReturn) {
        List<Statement> statements = IntStream.range(0, ctx.blockElement().size())
            .mapToObj(index -> parseBlockElement(ctx.blockElement(index), implicitReturn ? index == ctx.blockElement().size() - 1 : false))
            .collect(Collectors.toList());
        return evaluation ->
            statements.stream().filter(x -> {
                x.perform(evaluation);
                return !evaluation.hasReturned();
            }
        ).collect(Collectors.toList());
    }

    private static Statement parseBlockElement(ParserRuleContext ctx, boolean mustReturn) {
        return ctx.accept(new ReoBaseVisitor<Statement>() {
            @Override
            public Statement visitBlockElement(ReoParser.BlockElementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public Statement visitExpression(ReoParser.ExpressionContext ctx) {
                Expression expression = parseExpression(ctx);

                if(mustReturn)
                    return evaluation -> evaluation.returnValue(expression.perform(evaluation));
                else
                    return evaluation -> expression.perform(evaluation);
            }

            @Override
            public Statement visitStatement(ReoParser.StatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }
        });
    }

    private static Expression parseExpression(ParserRuleContext ctx) {
        return ctx.accept(new ReoBaseVisitor<Expression>() {
            @Override
            public Expression visitExpression(ReoParser.ExpressionContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public Expression visitExpression1(ReoParser.Expression1Context ctx) {
                Expression lhs = ctx.expression2().accept(this);

                for (ReoParser.Expression1Context rhsCtx : ctx.expression1()) {
                    Expression lhsTmp = lhs;
                    String selector = ctx.children.get(ctx.children.indexOf(rhsCtx) - 1).getText();
                    Expression rhs = rhsCtx.accept(this);
                    lhs = messageSend(lhsTmp, selector, Arrays.asList(rhs));
                }

                return lhs;
            }

            @Override
            public Expression visitExpression2(ReoParser.Expression2Context ctx) {
                Expression lhs = ctx.expression3().accept(this);

                for (ReoParser.Expression2Context rhsCtx : ctx.expression2()) {
                    Expression lhsTmp = lhs;
                    String selector = ctx.children.get(ctx.children.indexOf(rhsCtx) - 1).getText();
                    Expression rhs = rhsCtx.accept(this);
                    lhs = messageSend(lhsTmp, selector, Arrays.asList(rhs));
                }

                return lhs;
            }

            @Override
            public Expression visitExpression3(ReoParser.Expression3Context ctx) {
                return ctx.atom().accept(this);
            }

            private Expression messageSend(Expression target, String selector, List<Expression> arguments) {
                return evaluation -> {
                    RObject targetValue = target.perform(evaluation);
                    return targetValue.send(evaluation, selector, arguments.stream().map(x -> x.perform(evaluation)).collect(Collectors.toList()));
                };
            }

            @Override
            public Expression visitNumber(ReoParser.NumberContext ctx) {
                RObject valueTmp;

                try {
                    valueTmp =  new IntegerRObject(Long.parseLong(ctx.getText()));
                } catch (NumberFormatException e) {
                    valueTmp = new DoubleRObject(Double.parseDouble(ctx.getText()));
                }

                RObject value = valueTmp;

                return evaluation -> value;
            }

            @Override
            public Expression visitSelf(ReoParser.SelfContext ctx) {
                return evaluation -> evaluation.getReceiver();
            }
        });
    }
}
