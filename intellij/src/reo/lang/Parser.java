package reo.lang;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import reo.lang.antlr4.ReoLexer;
import reo.lang.antlr4.ReoParser;
import reo_OLD.runtime.Behavior;
import reo_OLD.runtime.Instructions;

import java.util.Hashtable;

public class Parser {
    public static Behavior parse(String text) {
        CharStream charStream = new ANTLRInputStream(text);
        ReoLexer lexer = new ReoLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ReoParser parser = new ReoParser(tokenStream);

        //return parseBlock(parser.block().statementOrExpression(), true, instructions -> instructions.add(Instructions.halt()), new Hashtable<>());

        return null;
    }
}
