package reo;

import reo.lang.Parser;
import reo.runtime.*;

public class Main {
    public static void main(String[] args) {
        /*String script =
            "var x\n" +
            "x = 1\n" +
            "x + 7 + 8";*/
        /*String script =
            "this.Integer.x = 7\n" +
            "this.Integer.x\n" +
            "";*/
        String script =
            "Integer.x = 7\n" +
            "Integer.x\n" +
            "";
        /*String script =
            //"this.Integer.+ = other => $addi(this, other)\n" +
            "this.Integer.+(other) => $addi(this, other)\n" +
            "4 + 7\n" +
            "";*/

        Behavior behavior = Parser.parse(script);
        //Behavior behavior = Parser.parse("this", true);

        Universe universe = new Universe();

        RObject result = universe.evaluate(behavior, universe.getAnyPrototype());
        System.out.println(result);
    }
}
