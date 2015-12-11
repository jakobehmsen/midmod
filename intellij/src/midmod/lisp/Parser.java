package midmod.lisp;

import midmod.lisp.antlr4.LispBaseVisitor;
import midmod.lisp.antlr4.LispLexer;
import midmod.lisp.antlr4.LispParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

public class Parser {
    private LispParser parser;

    public Parser(String sourceCode) throws IOException {
        this(new ByteArrayInputStream(sourceCode.getBytes()));
    }

    public Parser(InputStream sourceCode) throws IOException {
        CharStream charStream = new ANTLRInputStream(sourceCode);
        LispLexer lexer = new LispLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        parser = new LispParser(tokenStream);
    }

    public static Object parse(String sourceCode) throws IOException {
        return new Parser(sourceCode).parse();
    }

    public Object parse() {
        return parse(parser.element());
    }

    private Object parse(ParserRuleContext ctx) {
        return ctx.accept(new LispBaseVisitor<Object>() {
            @Override
            public Object visitList(LispParser.ListContext ctx) {
                return ctx.element().stream().map(x -> parse(x)).collect(Collectors.toList());
            }

            @Override
            public Object visitString(LispParser.StringContext ctx) {
                return ctx.getText().substring(1, ctx.getText().length() - 1);
            }

            @Override
            public Object visitWord(LispParser.WordContext ctx) {
                return ctx.getText();
            }

            @Override
            public Object visitNumber(LispParser.NumberContext ctx) {
                try {
                    return Integer.parseInt(ctx.getText());
                } catch (NumberFormatException e) {
                    return Double.parseDouble(ctx.getText());
                }
            }
        });
    }
}
