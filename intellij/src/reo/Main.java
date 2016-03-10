package reo;

import reo.lang.Parser;
import reo.runtime.*;

public class Main {
    public static void main(String[] args) {
        String script =
            "var x\n" +
            "x = 1\n" +
            "x + 7 + 8";

        Behavior behavior = Parser.parse(script);
        //Behavior behavior = Parser.parse("this", true);

        Universe universe = new Universe();
        CustomRObject world = new CustomRObject();
        world.put("Integer", universe.getIntegerPrototype());
        world.put("Array", universe.getArrayPrototype());
        world.put("Function", universe.getFunctionPrototype());

        RObject result = universe.evaluate(behavior, world);
        System.out.println(result);
    }
}
