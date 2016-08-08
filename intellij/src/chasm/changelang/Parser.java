package chasm.changelang;

import chasm.*;
import chasm.changelang.antlr4.ChangelangBaseVisitor;
import chasm.changelang.antlr4.ChangelangLexer;
import chasm.changelang.antlr4.ChangelangParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Parser {
    public static List<ChangeStatement> parse(String text) {
        CharStream charStream = new ANTLRInputStream(text);
        ChangelangLexer lexer = new ChangelangLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ChangelangParser parser = new ChangelangParser(tokenStream);

        return parse(parser.program());
    }

    private static List<ChangeStatement> parse(ChangelangParser.ProgramContext programContext) {
        return programContext.statement().stream().map(x -> parse(x)).collect(Collectors.toList());
    }

    private static ChangeStatement parse(ChangelangParser.StatementContext statementContext) {
        return statementContext.accept(new ChangelangBaseVisitor<ChangeStatement>() {
            @Override
            public ChangeStatement visitThisSlotAssign(ChangelangParser.ThisSlotAssignContext ctx) {
                ChangeExpression value = parse(ctx.expression());
                return new SlotAssignChangeStatement(new ThisChangeExpression(), parseId(ctx.identifier()), value);
            }

            @Override
            public ChangeStatement visitExpression(ChangelangParser.ExpressionContext ctx) {
                ChangeExpression expression = parseExpression(ctx);

                if(ctx.expressionSlotAssign() != null) {
                    ChangeExpression value = parse(ctx.expressionSlotAssign().expression());
                    return new SlotAssignChangeStatement(expression, parseId(ctx.expressionSlotAssign().identifier()), value);
                } else {
                    throw new IllegalArgumentException();
                }
            }
        });
    }

    private static ChangeExpression parseExpression(ChangelangParser.ExpressionContext ctx) {
        ChangeExpression expression = parse((ParserRuleContext) ctx.getChild(0));

        if(ctx.expressionSlotAccess() != null || ctx.expressionSlotAccess().size() > 0) {
            for (ChangelangParser.ExpressionSlotAccessContext expressionSlotAccessContext : ctx.expressionSlotAccess()) {
                expression = new SlotAccessChangeExpression(expression, parseId(expressionSlotAccessContext.identifier()));
            }
        }

        if(ctx.isClosedCapture != null)
            expression = new ClosedCaptureChangeExpression(expression, ctx.isClosedCapture.ID().getText(), createCapturedValueSupplier(ctx.isClosedCapture.isMulti != null, true));

        return expression;
    }

    private static Supplier<CapturedValue> createCapturedValueSupplier(boolean isMulti, boolean quote) {
        return () -> {
            if(isMulti)
                return new CapturedValue() {
                    List<Object> v = new ArrayList<>();

                    @Override
                    public void captureNext(Object value) {
                        if(!quote)
                            value = ((ChangeExpression)value).toValue();

                        v.add(value);
                    }

                    @Override
                    public Object buildValue() {
                        return v;
                    }
                };

            return new CapturedValue() {
                private Object v;

                @Override
                public void captureNext(Object value) {
                    if(!quote)
                        value = ((ChangeExpression)value).toValue();

                    v = value;
                }

                @Override
                public Object buildValue() {
                    return v;
                }
            };
        };
    }

    private static ChangeExpression parse(ParserRuleContext expressionContext) {
        return expressionContext.accept(new ChangelangBaseVisitor<ChangeExpression>() {
            @Override
            public ChangeExpression visitExpression(ChangelangParser.ExpressionContext ctx) {
                return parseExpression(ctx);
            }

            @Override
            public ChangeExpression visitIdentifier(ChangelangParser.IdentifierContext ctx) {
                if(ctx.isCapture == null)
                    return new SlotAccessChangeExpression(new ThisChangeExpression(), new SpecificIdChangeExpression(ctx.ID().getText()));

                return new CaptureChangeExpression(ctx.ID().getText(), createCapturedValueSupplier(ctx.isMulti != null, false));
            }

            @Override
            public ChangeExpression visitNumber(ChangelangParser.NumberContext ctx) {
                Number number;

                if(ctx.getText().contains("."))
                    number = Double.parseDouble(ctx.getText());
                else
                    number = Integer.parseInt(ctx.getText());

                return new ObjectChangeExpression(number);
            }

            @Override
            public ChangeExpression visitString(ChangelangParser.StringContext ctx) {
                return new ObjectChangeExpression(parseString(ctx.getText()));
            }

            @Override
            public ChangeExpression visitObjectLiteral(ChangelangParser.ObjectLiteralContext ctx) {
                return new ObjectLiteralChangeExpression(
                    ctx.objectLiteralSlot().stream().map(x -> new ObjectLiteralChangeExpression.Slot(x.identifier().getText(), parse(x.expression()))).collect(Collectors.toList())
                );
            }

            @Override
            public ChangeExpression visitArray(ChangelangParser.ArrayContext ctx) {
                return new ArrayChangeExpression(
                    ctx.expression().stream().map(x -> parse(x)).collect(Collectors.toList())
                );
            }

            @Override
            public ChangeExpression visitTemplateArray(ChangelangParser.TemplateArrayContext ctx) {
                return new TemplateArrayChangeExpression(parse(ctx.expression()));
            }
        });
    }

    private static IdChangeExpression parseId(ChangelangParser.IdentifierContext identifierContext) {
        if(identifierContext.isCapture == null)
            return new SpecificIdChangeExpression(identifierContext.ID().getText());
        return new CaptureIdExpression(identifierContext.ID().getText(), createCapturedValueSupplier(false, false));
    }

    private static String parseString(String text) {
        String rawString = text.substring(1, text.length() - 1);
        return rawString.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }
}
