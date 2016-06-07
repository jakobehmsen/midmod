package paidia;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.swing.*;
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
                TerminalNode operator = block.selector().ADD_OP() != null ? block.selector().ADD_OP() : block.selector().MUL_OP();
                return new CompositeValue(Arrays.asList("lhs", "rhs"), Arrays.asList(new ParameterCell(workspace), new ParameterCell(workspace)), block.selector().getText(), args -> {
                    return reduce(workspace, args, operator);
                }, workspace, block.selector().getText(), s -> s);
            }

            return null;
        } else {
            return parseBlockParts(workspace, block.blockPart());
        }
    }

    private static Value reduce(Workspace workspace, Value[] args, TerminalNode operator) {
        if(operator.getText().equals("+")) {
            Object result = (long)((AtomValue)args[0]).getValue() + (long)((AtomValue)args[1]).getValue();
            return new AtomValue(workspace, result.toString(), result.toString(), result);
        } else if(operator.getText().equals("-")) {
            Object result = (long)((AtomValue)args[0]).getValue() - (long)((AtomValue)args[1]).getValue();
            return new AtomValue(workspace, result.toString(), result.toString(), result);
        } else if(operator.getText().equals("*")) {
            Object result = (long)((AtomValue)args[0]).getValue() * (long)((AtomValue)args[1]).getValue();
            return new AtomValue(workspace, result.toString(), result.toString(), result);
        } else if(operator.getText().equals("/")) {
            Object result = (long)((AtomValue)args[0]).getValue() / (long)((AtomValue)args[1]).getValue();
            return new AtomValue(workspace, result.toString(), result.toString(), result);
        }

        return null;
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
                    return reduce(workspace, args, o.ADD_OP());

                    /*if(o.ADD_OP().getText().equals("+")) {
                        Object result = (long)((AtomValue)args[0]).getValue() + (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    } else if(o.ADD_OP().getText().equals("-")) {
                        Object result = (long)((AtomValue)args[0]).getValue() - (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    }

                    return null;*/
                });
            }

            @Override
            public Value visitMulExpression(PaidiaParser.MulExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.mulExpressionOp(), o -> o.MUL_OP().getText(), o -> o.chainedExpression(), (o, args) -> {
                    return reduce(workspace, args, o.MUL_OP());

                    /*if(o.MUL_OP().getText().equals("*")) {
                        Object result = (long)((AtomValue)args[0]).getValue() * (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    } else if(o.MUL_OP().getText().equals("/")) {
                        Object result = (long)((AtomValue)args[0]).getValue() / (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    }

                    return null;*/
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
                Value value = parseBlockPart(workspace, ctx.embeddedExpressionContent(), s -> s);
                return new EmbeddedValue(prefix, suffix, value);
                //return parseBlockPart(workspace, ctx.embeddedExpressionContent(), s -> prefix + s + suffix);
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

    public static JComponent parseComponent(String text) {
        CharStream charStream = new ANTLRInputStream(text);
        PaidiaLexer lexer = new PaidiaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PaidiaParser parser = new PaidiaParser(tokenStream);

        PaidiaParser.BlockContext block = parser.block();

        return parseComponentBlockParts(block.blockPart());
    }

    private static JComponent parseComponentBlockParts(List<PaidiaParser.BlockPartContext> blockPartContexts) {
        if (blockPartContexts.size() == 0)
            return null;
        else if (blockPartContexts.size() == 1) {
            return parseComponentBlockPart(blockPartContexts.get(0));
        } else {
            // Multiple parts.

            return null;
        }
    }

    private static JComponent parseComponentBlockPart(ParserRuleContext blockPartContext) {
        return blockPartContext.accept(new PaidiaBaseVisitor<JComponent>() {
            private <T extends ParserRuleContext> JComponent visitBinaryExpression(ParserRuleContext first, List<T> operands, Function<T, String> operatorGetter, Function<T, ParserRuleContext> operandGetter, BiFunction<T, Value[], Value> reducer) {
                JComponent value = parseComponentBlockPart(first);

                int start = first.stop.getStopIndex() + 1;
                for (T addExpressionOpContext : operands) {
                    ParserRuleContext operand = operandGetter.apply(addExpressionOpContext);
                    String operator = operatorGetter.apply(addExpressionOpContext);
                    int end = operand.start.getStartIndex() - 1;
                    JComponent lhs = value;
                    JComponent rhs = parseComponentBlockPart(operand);
                    String source = first.start.getInputStream().getText(new Interval(start, end));

                    TextContext textOperator;
                    if(operator.equals("+") || operator.equals("-"))
                        textOperator = new TextContext() {
                            @Override
                            public String getText(TextContext textContext, String text) {
                                return textContext.getTextAdd(text);
                            }

                            @Override
                            public String getTextAdd(String text) {
                                return text;
                            }

                            @Override
                            public String getTextMul(String text) {
                                return text;
                            }
                        };
                    else
                        textOperator = new TextContext() {
                            @Override
                            public String getText(TextContext textContext, String text) {
                                return textContext.getTextMul(text);
                            }

                            @Override
                            public String getTextAdd(String text) {
                                return "(" + text + ")";
                            }

                            @Override
                            public String getTextMul(String text) {
                                return text;
                            }
                        };

                    //value = new BinaryView(new Text(source, operator), textOperator, lhs, rhs);
                    value = new BinaryView(new Text(operator, operator), textOperator, lhs, rhs);
                    start = operand.stop.getStopIndex();
                }

                return value;
            }

            @Override
            public JComponent visitAddExpression(PaidiaParser.AddExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.addExpressionOp(), o -> o.ADD_OP().getText(), o -> o.mulExpression(), (o, args) -> {
                    return null;
                    //return reduce(workspace, args, o.ADD_OP());

                    /*if(o.ADD_OP().getText().equals("+")) {
                        Object result = (long)((AtomValue)args[0]).getValue() + (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    } else if(o.ADD_OP().getText().equals("-")) {
                        Object result = (long)((AtomValue)args[0]).getValue() - (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    }

                    return null;*/
                });
            }

            @Override
            public JComponent visitMulExpression(PaidiaParser.MulExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.mulExpressionOp(), o -> o.MUL_OP().getText(), o -> o.chainedExpression(), (o, args) -> {
                    return null;
                    //return reduce(workspace, args, o.MUL_OP());

                    /*if(o.MUL_OP().getText().equals("*")) {
                        Object result = (long)((AtomValue)args[0]).getValue() * (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    } else if(o.MUL_OP().getText().equals("/")) {
                        Object result = (long)((AtomValue)args[0]).getValue() / (long)((AtomValue)args[1]).getValue();
                        return new AtomValue(workspace, result.toString(), result.toString(), result);
                    }

                    return null;*/
                });
            }

            @Override
            public JComponent visitEmbeddedExpression(PaidiaParser.EmbeddedExpressionContext ctx) {
                return parseComponentBlockPart((ParserRuleContext) ctx.getChild(1));

                /*
                String prefix = ctx.start.getInputStream().getText(new Interval(
                    ((TerminalNode) ctx.getChild(0)).getSymbol().getStartIndex(),
                    ((ParserRuleContext) ctx.getChild(1)).start.getStartIndex() - 1
                ));
                String suffix = ctx.start.getInputStream().getText(new Interval(
                    ((ParserRuleContext) ctx.getChild(1)).stop.getStopIndex() + 1,
                    ((TerminalNode) ctx.getChild(2)).getSymbol().getStopIndex()
                ));
                Value value = parseBlockPart(workspace, ctx.embeddedExpressionContent(), s -> s);
                return new EmbeddedValue(prefix, suffix, value);
                */
            }

            @Override
            public JComponent visitNumber(PaidiaParser.NumberContext ctx) {
                Number number;

                try {
                    number = Long.parseLong(ctx.getText());
                } catch (NumberFormatException e) {
                    number = Double.parseDouble(ctx.getText());
                }

                return new AtomView(number.toString());
                //return new AtomValue(workspace, sourceWrapper.apply(ctx.getText()), ctx.getText(), number);
            }

            /*
            @Override
            public Value visitParameter(PaidiaParser.ParameterContext ctx) {
                return new ParameterCell(workspace);
            }
            */
        });
    }
}
