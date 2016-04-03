package yashl;

import yashl.runtime.Instructions;
import yashl.lang.Parser;
import yashl.runtime.Evaluation;
import yashl.runtime.Function;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //String sourceCode = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/yashl/core/bootstrap.ysl")));
        String sourceCode =
            "(set x 6)\n" +
            "(1)\n" +
            "x\n" +
            "";
        Object value = Parser.parse(sourceCode);
        Object result = Evaluation.evaluate(Function.compile(value, instructions -> instructions.add(Instructions.halt())));
        System.out.println(result);
    }
}
