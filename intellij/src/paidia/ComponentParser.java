package paidia;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ComponentParser {
    public static Value parse(Workspace workspace, String text) {
        CharStream charStream = new ANTLRInputStream(text);
        PaidiaLexer lexer = new PaidiaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PaidiaParser parser = new PaidiaParser(tokenStream);

        PaidiaParser.BlockContext block = parser.block();

        if(block.selector() != null) {
            /*if(block.selector().ADD_OP() != null || block.selector().MUL_OP() != null) {
                // Use ConstructorCell instead of ParameterCell?
                TerminalNode operator = block.selector().ADD_OP() != null ? block.selector().ADD_OP() : block.selector().MUL_OP();
                return new CompositeValue(Arrays.asList("lhs", "rhs"), Arrays.asList(new ParameterCell(workspace), new ParameterCell(workspace)), block.selector().getText(), args -> {
                    return reduce(workspace, args, operator);
                }, workspace, block.selector().getText(), s -> s);
            }*/

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
        } else if(operator.getText().equals("^")) {
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
                return visitBinaryExpression(ctx.lhs, ctx.mulExpressionOp(), o -> o.MUL_OP().getText(), o -> o.raiseExpression(), (o, args) -> {
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
            public Value visitRaiseExpression(PaidiaParser.RaiseExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.raiseExpressionOp(), o -> o.RAISE_OP().getText(), o -> o.chainedExpression(), (o, args) -> {
                    return reduce(workspace, args, o.RAISE_OP());
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

    public static JComponent parseComponent(String text, PlaygroundView playgroundView) {
        CharStream charStream = new ANTLRInputStream(text);
        PaidiaLexer lexer = new PaidiaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PaidiaParser parser = new PaidiaParser(tokenStream);

        PaidiaParser.BlockContext block = parser.block();

        if(block.selector() != null) {
            if(block.selector().binaryOperator() != null) {
                String operator = block.selector().binaryOperator().getText();
                TextContext textOperator = getBinaryTextOperator(operator);
                Function<ValueView[], ValueView> reducer = getBinaryReducer(operator);
                return new BinaryView(new Text(operator, operator), textOperator, playgroundView.createDefaultValueView(), playgroundView.createDefaultValueView(), reducer);
            } else if(block.selector().KW_IF() != null) {
                return new IfView(new AtomView("true", new Boolean(true)), (ValueView) playgroundView.createDefaultValueView(), (ValueView) playgroundView.createDefaultValueView());
            }
        }

        return parseComponentBlockParts(block.blockPart());
    }

    private static JComponent parseComponentBlockParts(List<PaidiaParser.BlockPartContext> blockPartContexts) {
        if (blockPartContexts.size() == 0)
            return null;
        else if (blockPartContexts.size() == 1) {
            return parseComponentBlockPart(blockPartContexts.get(0));
        } else {
            // Multiple parts.

            List<ValueView> expressions = blockPartContexts.stream().map(x -> (ValueView)parseComponentBlockPart(x)).collect(Collectors.toList());

            return new BlockView(expressions);
        }
    }

    private static List<Set<String>> operatorPrecedence = Arrays.asList(
        new String[]{"||"},
        new String[]{"&&"},
        new String[]{"==", "!="},
        new String[]{"<", ">", "<=", ">="},
        new String[]{"+", "-"},
        new String[]{"*", "/"},
        new String[]{"^"}
    ).stream().map(x -> new HashSet<>(Arrays.asList(x))).collect(Collectors.toList());

    private static int getOperatorPrecedence(String operator) {
        return IntStream.range(0, operatorPrecedence.size()).filter(i -> operatorPrecedence.get(i).contains(operator)).findFirst().getAsInt();
    }

    private static TextContext getBinaryTextOperator(String operator) {
        int precedence = getOperatorPrecedence(operator);

        return new TextContext() {
            @Override
            public String getText(TextContext textContext, String text) {
                return textContext.getTextOperator(text, operator, precedence);
            }

            @Override
            public String getTextAdd(String text) {
                return null;
            }

            @Override
            public String getTextMul(String text) {
                return null;
            }

            @Override
            public String getTextRaise(String text) {
                return null;
            }

            @Override
            public String getTextOperator(String text, String otherOperator, int otherPrecedence) {
                if(otherPrecedence < precedence)
                    return "(" + text + ")";

                return text;
            }
        };
    }

    private static Hashtable<String, Function<ValueView[], ValueView>> binaryReducers = new Hashtable<>();

    private static <T> void addBinaryReducer(String operator, Function<ValueView[], ValueView> reducer) {
        binaryReducers.put(operator, reducer);
    }

    private static <T> void addBinaryNumberReducer(String operator, BiFunction<BigDecimal, BigDecimal, T> numberReducer) {
        addBinaryReducer(operator, args -> {
            T result = numberReducer.apply(((BigDecimal)((AtomView)args[0]).getValue()), ((BigDecimal)((AtomView)args[1]).getValue()));
            return new AtomView(result.toString(), result);
        });
    }

    private static <T> void addBinaryBooleanReducer(String operator, BiFunction<Boolean, Boolean, T> numberReducer) {
        addBinaryReducer(operator, args -> {
            T result = numberReducer.apply(((Boolean)((AtomView)args[0]).getValue()), ((Boolean)((AtomView)args[1]).getValue()));
            return new AtomView(result.toString(), result);
        });
    }

    static {
        addBinaryBooleanReducer("||", (x, y) -> x || y);
        addBinaryBooleanReducer("&&", (x, y) -> x && y);
        addBinaryNumberReducer("==", (x, y) -> x.equals(y));
        addBinaryNumberReducer("!=", (x, y) -> !x.equals(y));
        addBinaryNumberReducer("<", (x, y) -> x.compareTo(y) < 0);
        addBinaryNumberReducer(">", (x, y) -> x.compareTo(y) > 0);
        addBinaryNumberReducer("<=", (x, y) -> x.compareTo(y) <= 0);
        addBinaryNumberReducer(">=", (x, y) -> x.compareTo(y) >= 0);
        addBinaryNumberReducer("+", (x, y) -> x.add(y));
        addBinaryNumberReducer("-", (x, y) -> x.subtract(y));
        addBinaryNumberReducer("*", (x, y) -> x.multiply(y));
        addBinaryNumberReducer("/", (x, y) -> x.divide(y, MathContext.DECIMAL128));
        addBinaryNumberReducer("^", (x, y) -> x.pow(y.intValue()));
    }

    private static Function<ValueView[], ValueView> getBinaryReducer(String operator) {
        return binaryReducers.get(operator);
    }

    private static JComponent parseComponentBlockPart(ParserRuleContext blockPartContext) {
        return blockPartContext.accept(new PaidiaBaseVisitor<JComponent>() {
            @Override
            public JComponent visitAssignment(PaidiaParser.AssignmentContext ctx) {
                String id = ctx.ID().getText();
                ValueView value = (ValueView) parseComponentBlockPart(ctx.expression());

                return new AssignmentView(id, value);
            }

            @Override
            public JComponent visitIfExpression(PaidiaParser.IfExpressionContext ctx) {
                JComponent condition = parseComponentBlockPart(ctx.condition);
                JComponent trueExpression = parseComponentBlockPart(ctx.trueExpression);
                JComponent falseExpression = parseComponentBlockPart(ctx.falseExpression);

                return new IfView((ValueView) condition, (ValueView)trueExpression, (ValueView)falseExpression);
            }

            private <T extends ParserRuleContext> JComponent visitBinaryExpression(ParserRuleContext first, List<T> operands, Function<T, String> operatorGetter, Function<T, ParserRuleContext> operandGetter) {
                JComponent value = parseComponentBlockPart(first);

                int start = first.stop.getStopIndex() + 1;
                for (T addExpressionOpContext : operands) {
                    ParserRuleContext operand = operandGetter.apply(addExpressionOpContext);
                    String operator = operatorGetter.apply(addExpressionOpContext);
                    int end = operand.start.getStartIndex() - 1;
                    JComponent lhs = value;
                    JComponent rhs = parseComponentBlockPart(operand);
                    String source = first.start.getInputStream().getText(new Interval(start, end));

                    TextContext textOperator = getBinaryTextOperator(operator);
                    Function<ValueView[], ValueView> reducer = getBinaryReducer(operator);

                    //value = new BinaryView(new Text(source, operator), textOperator, lhs, rhs);
                    value = new BinaryView(new Text(operator, operator), textOperator, lhs, rhs, reducer);
                    start = operand.stop.getStopIndex();
                }

                return value;
            }

            @Override
            public JComponent visitLogicalOrExpression(PaidiaParser.LogicalOrExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.logicalOrExpressionOp(), o -> o.OR_OP().getText(), o -> o.logicalAndExpression());
            }

            @Override
            public JComponent visitLogicalAndExpression(PaidiaParser.LogicalAndExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.logicalAndExpressionOp(), o -> o.AND_OP().getText(), o -> o.equalityExpression());
            }

            @Override
            public JComponent visitEqualityExpression(PaidiaParser.EqualityExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.equalityExpressionOp(), o -> o.EQ_OP().getText(), o -> o.relationalExpression());
            }

            @Override
            public JComponent visitRelationalExpression(PaidiaParser.RelationalExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.relationalExpressionOp(), o -> o.REL_OP().getText(), o -> o.addExpression());
            }

            @Override
            public JComponent visitAddExpression(PaidiaParser.AddExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.addExpressionOp(), o -> o.ADD_OP().getText(), o -> o.mulExpression());
            }

            @Override
            public JComponent visitMulExpression(PaidiaParser.MulExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.mulExpressionOp(), o -> o.MUL_OP().getText(), o -> o.raiseExpression());
            }

            @Override
            public JComponent visitRaiseExpression(PaidiaParser.RaiseExpressionContext ctx) {
                return visitBinaryExpression(ctx.lhs, ctx.raiseExpressionOp(), o -> o.RAISE_OP().getText(), o -> o.chainedExpression());
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
                BigDecimal number;

                try {
                    number = BigDecimal.valueOf(Long.parseLong(ctx.getText()));
                } catch (NumberFormatException e) {
                    number = BigDecimal.valueOf(Double.parseDouble(ctx.getText()));
                }

                return new AtomView(ctx.getText(), number);
            }

            @Override
            public JComponent visitIdentifier(PaidiaParser.IdentifierContext ctx) {
                String name = ctx.getText();

                return new IdentifierView(name);
            }
        });
    }
}
