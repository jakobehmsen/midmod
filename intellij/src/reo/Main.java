package reo;

import reo.lang.Parser;
import reo.runtime.*;

public class Main {
    public static void main(String[] args) {
        /*String script =
            "var x\n" +
            "x = 1\n" +
            "x + 7 + 8";*/
        String script =
            "this.Integer.x = 7\n" +
            "this.Integer.x\n" +
            "";

        Behavior behavior = Parser.parse(script);
        //Behavior behavior = Parser.parse("this", true);

        Universe universe = new Universe();
        universe.getAnyPrototype().put("Integer", universe.getIntegerPrototype());
        universe.getAnyPrototype().put("Array", universe.getArrayPrototype());
        universe.getAnyPrototype().put("Function", universe.getFunctionPrototype());

        RObject result = universe.evaluate(behavior, universe.getAnyPrototype());
        System.out.println(result);
    }
}
