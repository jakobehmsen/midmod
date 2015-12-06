package midmod.json;

import midmod.MapCell;
import midmod.json.antlr4.JSONBaseListener;
import midmod.json.antlr4.JSONBaseVisitor;
import midmod.json.antlr4.JSONLexer;
import midmod.json.antlr4.JSONParser;
import org.antlr.v4.runtime.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class Parser {
    private JSONParser parser;

    public Parser(String sourceCode) throws IOException {
        this(new ByteArrayInputStream(sourceCode.getBytes()));
    }

    public Parser(InputStream sourceCode) throws IOException {
        CharStream charStream = new ANTLRInputStream(sourceCode);
        JSONLexer lexer = new JSONLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        parser = new JSONParser(tokenStream);
    }

    public Object execute(MapCell self) {
        Object result = null;

        for (JSONParser.JsonContext ctx : parser.script().json()) {
            result = execute(self, ctx);
        }

        return result;
    }

    private Object execute(MapCell self, JSONParser.JsonContext ctx) {
        return ctx.accept(new JSONBaseVisitor<Object>() {
            @Override
            public Object visitValue(JSONParser.ValueContext ctx) {
                return execute(self, ctx);
            }

            @Override
            public Object visitPair(JSONParser.PairContext ctx) {
                return execute(self, ctx);
            }
        });
    }

    private Object execute(MapCell self, JSONParser.PairContext ctx) {
        String name = ctx.ID().getText();
        Object value = execute(self, ctx.value());
        self.put(name, value);
        return value;
    }

    private Object execute(MapCell self, JSONParser.ValueContext ctx) {
        return ctx.getChild(0).accept(new JSONBaseVisitor<Object>() {
            @Override
            public Object visitIdentifier(JSONParser.IdentifierContext ctx) {
                String name = ctx.ID().getText();
                return self.get(name);
            }

            @Override
            public Object visitString(JSONParser.StringContext ctx) {
                return ctx.getText().substring(1, ctx.getText().length() - 1);
            }

            @Override
            public Object visitNumber(JSONParser.NumberContext ctx) {
                return Double.parseDouble(ctx.getText());
            }

            @Override
            public Object visitObject(JSONParser.ObjectContext ctx) {
                MapCell object = new MapCell();

                ctx.pair().forEach(pairCtx -> execute(object, pairCtx));

                return object;
            }
        });
    }
}
