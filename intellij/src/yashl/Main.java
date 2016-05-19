package yashl;

import yashl.runtime.Instructions;
import yashl.lang.Parser;
import yashl.runtime.Evaluation;
import yashl.runtime.Function;
import yashl.runtime.Symbol;

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
            "(set this double-of (lambda (x) (addi x x)))\n" +
            //"(apply this (get this double-of) 8)\n" +
            "((get this double-of) 8)\n" +
            "";
        Object value = Parser.parse(sourceCode);
        Hashtable<Symbol, Integer> locals = new Hashtable<>();
        locals.put(Symbol.get("this"), 0);
        Object result = Evaluation.evaluate(Function.compile(locals, value, instructions -> instructions.add(Instructions.halt())));
        System.out.println(result);
    }
}
