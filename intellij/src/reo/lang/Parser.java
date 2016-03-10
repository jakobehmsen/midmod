package reo.lang;

import jsflow.bindlang.antlr4.ReoBaseVisitor;
import jsflow.bindlang.antlr4.ReoLexer;
import jsflow.bindlang.antlr4.ReoParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import reo.runtime.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class Parser {
    public static Behavior parse(String text, boolean implicitReturn) {
        CharStream charStream = new ANTLRInputStream(text);
        ReoLexer lexer = new ReoLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReoParser parser = new ReoParser(tokenStream);

        return parseBlock(parser.block(), implicitReturn);
    }

    private static Behavior parseBlock(ReoParser.BlockContext ctx, boolean implicitReturn) {
        List<Consumer<List<Instruction>>> emitters = new ArrayList<>();

        IntStream.range(0, ctx.blockElement().size()).forEach(index ->
            parseBlockElement(ctx.blockElement(index), implicitReturn ? index == ctx.blockElement().size() - 1 : false, emitters)
        );

        if(implicitReturn)
            emitters.add(instructions -> instructions.add(Instructions.halt()));

        ArrayList<Instruction> instructions = new ArrayList<>();

        emitters.forEach(e -> e.accept(instructions));

        return new Behavior(instructions.toArray(new Instruction[instructions.size()]));
    }

    private static void parseBlockElement(ParserRuleContext ctx, boolean mustReturn, List<Consumer<List<Instruction>>> emitters) {
        ctx.accept(new ReoBaseVisitor<Void>() {
            @Override
            public Void visitBlockElement(ReoParser.BlockElementContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public Void visitExpression(ReoParser.ExpressionContext ctx) {
                parseExpression(ctx, emitters);

                return null;
            }

            @Override
            public Void visitStatement(ReoParser.StatementContext ctx) {
                return ctx.getChild(0).accept(this);
            }
        });
    }

    private static void parseExpression(ParserRuleContext ctx, List<Consumer<List<Instruction>>> emitters) {
        ctx.accept(new ReoBaseVisitor<Void>() {
            @Override
            public Void visitExpression(ReoParser.ExpressionContext ctx) {
                return ctx.getChild(0).accept(this);
            }

            @Override
            public Void visitExpression1(ReoParser.Expression1Context ctx) {
                ctx.expression2().accept(this);

                for (ReoParser.Expression1Context rhsCtx : ctx.expression1()) {
                    String selector = ctx.children.get(ctx.children.indexOf(rhsCtx) - 1).getText();
                    rhsCtx.accept(this);
                    messageSend(selector, 1, emitters);
                }

                return null;
            }

            @Override
            public Void visitExpression2(ReoParser.Expression2Context ctx) {
                ctx.expression3().accept(this);

                for (ReoParser.Expression2Context rhsCtx : ctx.expression2()) {
                    String selector = ctx.children.get(ctx.children.indexOf(rhsCtx) - 1).getText();
                    messageSend(selector, 1, emitters);
                }

                return null;
            }

            @Override
            public Void visitExpression3(ReoParser.Expression3Context ctx) {
                ctx.atom().accept(this);

                return null;
            }

            private Void messageSend(String selector, int arity, List<Consumer<List<Instruction>>> emitters) {
                emitters.add(instructions -> instructions.add(Instructions.send(selector, arity)));

                return null;
            }

            @Override
            public Void visitNumber(ReoParser.NumberContext ctx) {

                RObject valueTmp;

                try {
                    valueTmp =  new IntegerRObject(Long.parseLong(ctx.getText()));
                } catch (NumberFormatException e) {
                    valueTmp = new DoubleRObject(Double.parseDouble(ctx.getText()));
                }

                RObject value = valueTmp;

                emitters.add(instructions -> instructions.add(Instructions.loadConst(value)));

                return null;
            }

            @Override
            public Void visitSelf(ReoParser.SelfContext ctx) {
                emitters.add(instructions -> instructions.add(Instructions.loadLocal(0)));

                return null;
            }
        });
    }
}
