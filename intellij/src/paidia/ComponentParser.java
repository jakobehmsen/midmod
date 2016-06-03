package paidia;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ComponentParser {
    public static Value parse(Workspace workspace, String text) {
        CharStream charStream = new ANTLRInputStream(text);
        PaidiaLexer lexer = new PaidiaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PaidiaParser parser = new PaidiaParser(tokenStream);

        PaidiaParser.BlockContext block = parser.block();

        if(block.selector() != null) {
            if(block.selector().ADD_OP() != null || block.selector().MUL_OP() != null) {
                // Use ConstructorCell instead of ParameterCell?
                return new CompositeValue(Arrays.asList("lhs", "rhs"), Arrays.asList(new ParameterCell(workspace), new ParameterCell(workspace)), block.selector().getText(), args -> {
                    return null;
                }, workspace, block.selector().getText(), s -> s);
            }

            return null;
        } else {
            return parseBlockParts(workspace, block.blockPart());
        }
    }

    private static Value parseBlockParts(Workspace workspace, List<PaidiaParser.BlockPartContext> blockPartContexts) {
        if (blockPartContexts.size() == 0)
            return null;
        else if (blockPartContexts.size() == 1) {
            return parseBlockPart(workspace, blockPartContexts.get(0), s -> s);
        } else {
            // Multiple parts.

            return null;
        }
    }

    private static Value parseBlockPart(Workspace workspace, ParserRuleContext blockPartContext, Function<String, String> sourceWrapper) {
        return blockPartContext.accept(new PaidiaBaseVisitor<Value>() {
            private <T extends ParserRuleContext> Value visitBinaryExpression(ParserRuleContext first, List<T> operands, Function<T, String> operatorGetter, Function<T, ParserRuleContext> operandGetter, BiFunction<T, Value[], Value> reducer) {
                Value value = parseBlockPart(workspace, first, s -> operands.size() > 0 ? s : sourceWrapper.apply(s));

                int start = first.stop.getStopIndex() + 1;
                for (T addExpressionOpContext : operands) {
                    ParserRuleContext operand = operandGetter.apply(addExpressionOpContext);
                    String operator = operatorGetter.apply(addExpressionOpContext);
                    int end = operand.start.getStartIndex() - 1;
                    Value lhs = value;
                    Value rhs = parseBlockPart(workspace, operand, s -> s);
                    String source = first.start.getInputStream().getText(new Interval(start, end));
                    boolean isLast = operands.indexOf(addExpressionOpContext) == operands.size() - 1;
                    Function<String, String> sr = s -> s;
                    if(isLast)
                        sr = sourceWrapper;
                    value = new CompositeValue(Arrays.asList("lhs", "rhs"), Arrays.asList(lhs, rhs), operator, args -> reducer.apply(addExpressionOpContext, args), workspace, source, sr);
                    start = operand.stop.getStopIndex();
                }

                return value;
            }

            @Override
            public Value visitAddExpression(PaidiaParser.AddExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.addExpressionOp(), o -> o.ADD_OP().getText(), o -> o.mulExpression(), (o, args) -> {
                    if(o.ADD_OP().getText().equals("+")) {
                        Object result = (long)((AtomValue)args[0]).getValue() + (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    } else if(o.ADD_OP().getText().equals("-")) {
                        Object result = (long)((AtomValue)args[0]).getValue() - (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    }

                    return null;
                });
            }

            @Override
            public Value visitMulExpression(PaidiaParser.MulExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.mulExpressionOp(), o -> o.MUL_OP().getText(), o -> o.chainedExpression(), (o, args) -> {
                    if(o.MUL_OP().getText().equals("*")) {
                        Object result = (long)((AtomValue)args[0]).getValue() * (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    } else if(o.MUL_OP().getText().equals("/")) {
                        Object result = (long)((AtomValue)args[0]).getValue() / (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    }

                    return null;
                });
            }

            @Override
            public Value visitEmbeddedExpression(PaidiaParser.EmbeddedExpressionContext ctx) {
                String prefix = ctx.start.getInputStream().getText(new Interval(
                    ((TerminalNode) ctx.getChild(0)).getSymbol().getStartIndex(),
                    ((ParserRuleContext) ctx.getChild(1)).start.getStartIndex() - 1
                ));
                String suffix = ctx.start.getInputStream().getText(new Interval(
                    ((ParserRuleContext) ctx.getChild(1)).stop.getStopIndex() + 1,
                    ((TerminalNode) ctx.getChild(2)).getSymbol().getStopIndex()
                ));
                return parseBlockPart(workspace, ctx.embeddedExpressionContent(), s -> prefix + s + suffix);
            }

            @Override
            public Value visitNumber(PaidiaParser.NumberContext ctx) {
                Number number;

                try {
                    number = Long.parseLong(ctx.getText());
                } catch (NumberFormatException e) {
                    number = Double.parseDouble(ctx.getText());
                }

                return new AtomValue(workspace, sourceWrapper.apply(ctx.getText()), ctx.getText(), number);
            }

            @Override
            public Value visitParameter(PaidiaParser.ParameterContext ctx) {
                return new ParameterCell(workspace);
            }
        });
    }
}
