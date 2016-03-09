package reo;

import reo.lang.Parser;
import reo.runtime.*;

public class Main {
    public static void main(String[] args) {
        Behavior behavior = Parser.parse("1 + 5", true);
        //Behavior behavior = Parser.parse("this", true);

        Universe universe = new Universe();
        CustomRObject world = new CustomRObject();
        world.put("Integer", universe.getIntegerPrototype());
        world.put("Array", universe.getArrayPrototype());
        world.put("Function", universe.getFunctionPrototype());

        RObject result = universe.evaluate(behavior, world);
        result.toString();
    }
}
