package yashl.lang;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import yashl.lang.antlr4.YashlBaseVisitor;
import yashl.lang.antlr4.YashlLexer;
import yashl.lang.antlr4.YashlParser;
import yashl.runtime.ConsCell;
import yashl.runtime.Symbol;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {
    public static Object parse(String text) {
        CharStream charStream = new ANTLRInputStream(text);
        YashlLexer lexer = new YashlLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        YashlParser parser = new YashlParser(tokenStream);

        List<Object> forms = parser.forms().form().stream().map(x -> parseForm(x)).collect(Collectors.toList());

        return ConsCell.cons(forms);
    }

    private static Object parseForm(YashlParser.FormContext form) {
        return form.accept(new YashlBaseVisitor<Object>() {
            @Override
            public Object visitList(YashlParser.ListContext ctx) {
                return ConsCell.cons(ctx.form().stream().map(x -> parseForm(x)).collect(Collectors.toList()));
            }

            @Override
            public Object visitNumber(YashlParser.NumberContext ctx) {
                try {
                    return Long.parseLong(ctx.getText());
                } catch (NumberFormatException e) {
                    return Double.parseDouble(ctx.getText());
                }
            }

            @Override
            public Object visitString(YashlParser.StringContext ctx) {
                String rawString = ctx.getText().substring(1, ctx.getText().length() - 1);
                return rawString.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
            }

            @Override
            public Object visitSymbol(YashlParser.SymbolContext ctx) {
                return Symbol.get(ctx.getText());
            }
        });
    }
}
