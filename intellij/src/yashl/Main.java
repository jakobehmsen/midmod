package yashl;

import yashl.runtime.Instructions;
import yashl.lang.Parser;
import yashl.runtime.Evaluation;
import yashl.runtime.Function;

import java.io.IOException;
import java.util.Hashtable;

public class Main {
    public static void main(String[] args) throws IOException {
        //String sourceCode = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/yashl/core/bootstrap.ysl")));
        String sourceCode =
            /*"(set x 6)\n" +
            "(1)\n" +
            "x\n" +
            "(quote (x y z))\n" +
            "(cons x (cons (quote y) ()))\n" +
            ";(1 2 3)\n" +
            "(set myList (quote (x y z)))\n" +
            "(cdr myList)\n" +*/
            "(set f (lambda (x) x))\n" +
            "(f 8)\n" +
            "";
        Object value = Parser.parse(sourceCode);
        Object result = Evaluation.evaluate(Function.compile(new Hashtable<>(), value, instructions -> instructions.add(Instructions.halt())));
        System.out.println(result);
    }
}
