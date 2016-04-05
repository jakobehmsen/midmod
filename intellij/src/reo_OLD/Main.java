package reo_OLD;

import reo_OLD.lang.Parser;
import reo_OLD.runtime.*;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        /*String script =
            "var x\n" +
            "x = 1\n" +
            "x + 7 + 8";*/
        /*String script =
            "this.Integer.x = 7\n" +
            "this.Integer.x\n" +
            "";*/
        /*String script =
            "Integer.x = 7\n" +
            "Integer.x\n" +
            "";*/
        /*String script =
            //"Integer.+(other) => { return $addi(this, other) }\n" +
            "Integer.+(other) => $addi(this, other)\n" +
            "4 + 7\n" +
            "";*/
        /*String script =
            "(other => $addi(this, other))\n" +
            "";*/
        /*String script =
            //"Integer.+(other) => { return $addi(this, other) }\n" +
            "String.toString() => \"asdf\"\n" +
            "String.toString(other) => other\n" +
            "\"MyString\".toString(\"sdf\").toString()\n" +
            "";*/
        /*String script =
            "Array.[](index) => $geta(this, index)\n" +
            "Array.[]=(index, value) => { $seta(this, index, value) return value }\n" +
            "var arr = #[1, 2, 3, 4, 5]\n" +
            "arr[3] = 7\n" +
            "arr[3]\n" +
            "";*/
        /*String script =
            "#{x = 5 y = 6 toString() => \"Whatever\"}.toString()\n" +
            "";*/

        String script = new String(java.nio.file.Files.readAllBytes(Paths.get("src/reo_OLD/core/bootstrap.reo")));

        Behavior behavior = Parser.parse(script);
        //Behavior behavior = Parser.parse("this", true);

        Universe universe = new Universe();

        RObject result = universe.evaluate(behavior, universe.getAnyPrototype());
        System.out.println(result);
    }
}
