package reo;

import reo.lang.Parser;
import reo.runtime.Evaluation;
import reo.runtime.RObject;
import reo.runtime.Statement;
import reo.runtime.Universe;

public class Main {
    public static void main(String[] args) {
        Statement statement = Parser.parse("1 + 5", true);
        Universe universe = new Universe();
        RObject result = universe.evaluate(statement);
        result.toString();
    }
}
