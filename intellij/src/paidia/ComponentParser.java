package paidia;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import java.util.Arrays;
import java.util.List;
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
                return new CompositeValue(Arrays.asList("lhs", "rhs"), Arrays.asList(new ParameterCell(workspace), new ParameterCell(workspace)), block.selector().getText(), args -> {
                    return null;
                }, workspace, block.selector().getText());
            }

            /*JLabel view = new JLabel(block.selector().getText());
            view.setSize(((ComponentUI) view.getUI()).getPreferredSize(view));
            return view;*/

            return null;
        } else {
            return parseBlockParts(workspace, block.blockPart());
        }
    }

    private static Value parseBlockParts(Workspace workspace, List<PaidiaParser.BlockPartContext> blockPartContexts) {
        if (blockPartContexts.size() == 0)
            return null;
        else if (blockPartContexts.size() == 1) {
            return parseBlockPart(workspace, blockPartContexts.get(0));
        } else {
            // Multiple parts.

            return null;
        }
    }

    private static Value parseBlockPart(Workspace workspace, ParserRuleContext blockPartContext) {
        return blockPartContext.accept(new PaidiaBaseVisitor<Value>() {
            private <T extends ParserRuleContext> Value visitBinaryExpression(ParserRuleContext first, List<T> operands, Function<T, String> operatorGetter, Function<T, ParserRuleContext> operandGetter) {
                Value value = parseBlockPart(workspace, first);

                int start = first.stop.getStopIndex() + 1;
                for (T addExpressionOpContext : operands) {
                    ParserRuleContext operand = operandGetter.apply(addExpressionOpContext);
                    String operator = operatorGetter.apply(addExpressionOpContext);
                    int end = operand.start.getStartIndex() - 1;
                    Value lhs = value;
                    Value rhs = parseBlockPart(workspace, operand);
                    String source = first.start.getInputStream().getText(new Interval(start, end));
                    value = new CompositeValue(Arrays.asList("lhs", "rhs"), Arrays.asList(lhs, rhs), operator, args -> {
                        return null;
                    }, workspace, source);
                    start = operand.stop.getStopIndex();
                }

                return value;
            }

            @Override
            public Value visitAddExpression(PaidiaParser.AddExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.addExpressionOp(), o -> o.ADD_OP().getText(), o -> o.mulExpression());
            }

            @Override
            public Value visitMulExpression(PaidiaParser.MulExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.mulExpressionOp(), o -> o.MUL_OP().getText(), o -> o.chainedExpression());
            }

            @Override
            public Value visitNumber(PaidiaParser.NumberContext ctx) {
                Number number;

                try {
                    number = Long.parseLong(ctx.getText());
                } catch (NumberFormatException e) {
                    number = Double.parseDouble(ctx.getText());
                }

                return new AtomValue(workspace, ctx.getText(), number);
            }

            @Override
            public Value visitParameter(PaidiaParser.ParameterContext ctx) {
                return new ParameterCell(workspace);
            }
        });
    }
}
