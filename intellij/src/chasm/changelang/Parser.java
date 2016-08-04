package chasm.changelang;

import chasm.*;
import chasm.changelang.antlr4.ChangelangBaseVisitor;
import chasm.changelang.antlr4.ChangelangLexer;
import chasm.changelang.antlr4.ChangelangParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;
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
                return new SlotAssignChangeStatement(
                    new ThisChangeExpression(),
                    new SpecificIdChangeExpression(ctx.identifier().getText()),
                    value);
            }

            @Override
            public ChangeStatement visitExpression(ChangelangParser.ExpressionContext ctx) {
                ChangeExpression expression = parse((ParserRuleContext) ctx.getChild(0));

                if(ctx.expressionSlotAccess() != null || ctx.expressionSlotAccess().size() > 0) {

                }

                if(ctx.expressionSlotAssign() != null) {
                    ChangeExpression value = parse(ctx.expressionSlotAssign().expression());
                    return new SlotAssignChangeStatement(expression, new SpecificIdChangeExpression(ctx.expressionSlotAssign().identifier().getText()), value);
                } else {
                    throw new IllegalArgumentException();
                }
            }
        });
    }

    private static ChangeExpression parse(ParserRuleContext expressionContext) {
        return expressionContext.accept(new ChangelangBaseVisitor<ChangeExpression>() {
            @Override
            public ChangeExpression visitIdentifier(ChangelangParser.IdentifierContext ctx) {
                return new SlotAccessChangeExpression(new ThisChangeExpression(), new SpecificIdChangeExpression(ctx.ID().getText()));
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
        });
    }

    private static String parseString(String text) {
        String rawString = text.substring(1, text.length() - 1);
        return rawString.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
    }
}
