package reo;

import reo.lang.Parser;
import reo.runtime.CustomRObject;
import reo.runtime.RObject;
import reo.runtime.Statement;
import reo.runtime.Universe;

public class Main {
    public static void main(String[] args) {
        //Statement statement = Parser.parse("1 + 5", true);
        Statement statement = Parser.parse("this", true);

        Universe universe = new Universe();
        CustomRObject world = new CustomRObject();
        world.put("Integer", universe.getIntegerPrototype());
        world.put("Array", universe.getArrayPrototype());
        world.put("Function", universe.getFunctionPrototype());

        RObject result = universe.evaluate(statement, world);
        result.toString();
    }
}
