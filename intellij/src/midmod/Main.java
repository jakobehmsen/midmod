package midmod;

import midmod.json.Parser;
import midmod.rules.RuleMap;
import midmod.rules.actions.Action;
import midmod.rules.patterns.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    private static int nativeActions;

    private static Action toNative(Object code) {
        ClassNode classNode = new ClassNode(Opcodes.ASM5);

        populateNativeCode(classNode, code);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PrintWriter ps = new PrintWriter(os);
        classNode.accept(new TraceClassVisitor(new PrintWriter(System.out)));
        org.objectweb.asm.util.CheckClassAdapter.verify(new org.objectweb.asm.ClassReader(classWriter.toByteArray()), true, ps);

        byte[] bytes = classWriter.toByteArray();

        String name = "NativeAction" + nativeActions++;

        try {
            return (Action)Class.forName(name, false, new SingleClassLoader(name, bytes)).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void populateNativeCode(ClassNode classNode, Object code) {
        if(code instanceof List) {
            List<Object> codeAsList = (List<Object>)code;

            // Dangerous
            String operator = (String)codeAsList.get(0);

            switch (operator) {
                case "+i":
                    break;
            }
        }
    }

    public static void main(String[] args) throws IOException {

        //Object value = midmod.lisp.Parser.parse("(\"print\" \"some string\")");

        RuleMap rules = new RuleMap();

        rules.define(
            Patterns.conformsTo(Patterns.equalsObject("+"),
                Patterns.is(String.class).andThen(Patterns.capture("lhs")),
                Patterns.is(String.class).andThen(Patterns.capture("rhs"))
            ),
            (ruleMap, captures) -> (String)captures.get("lhs") + (String)captures.get("rhs")
        );

        rules.define(
            Patterns.conformsTo(Patterns.equalsObject("toNative"),
                Patterns.is(String.class).andThen(Patterns.capture("code"))
            ),
            (ruleMap, captures) -> toNative(captures.get("code"))
        );

        rules.define(
            Patterns.conformsTo(
                Patterns.equalsObject("class"),
                Patterns.is(String.class).andThen(Patterns.capture("name"))
            ),
            (ruleMap, captures) -> {
                try {
                    return Class.forName((String) captures.get("name"));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                return null; // Should be an error; how to handle errors? First class frames?
            }
        );
        rules.define(
            Patterns.conformsTo(
                Patterns.equalsObject("invoke"),
                Patterns.is(Class.class).andThen(Patterns.capture("class")),
                Patterns.is(Object.class).andThen(Patterns.capture("instance")),
                Patterns.is(String.class).andThen(Patterns.capture("methodName")),
                Patterns.is(List.class).andThen(Patterns.capture("parameterTypes")),
                Patterns.is(List.class).andThen(Patterns.capture("arguments"))
            ),
            (ruleMap, captures) -> {
                try {
                    Class[] parameterTypes = ((List<Object>) captures.get("parameterTypes")).stream().toArray(s -> new Class[s]);
                    Method method = ((Class<?>) captures.get("class")).getMethod((String) captures.get("methodName"), parameterTypes);
                    Object[] arguments = ((List<Object>) captures.get("arguments")).stream().toArray(s -> new Object[s]);
                    return method.invoke(captures.get("instance"), arguments);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null; // Should be an error; how to handle errors? First class frames?
            }
        );
        rules.define(
            Patterns.conformsTo(
                Patterns.equalsObject("+"),
                Patterns.is(Double.class).andThen(Patterns.capture("lhs")),
                Patterns.is(Double.class).andThen(Patterns.capture("rhs"))
            ),
            (ruleMap, captures) -> (double)captures.get("lhs") + (double)captures.get("rhs")
        );
        rules.define(
            Patterns.conformsTo(
                Patterns.equalsObject("+"),
                Patterns.is(Double.class).andThen(Patterns.capture("lhs")),
                Patterns.is(Integer.class).andThen(Patterns.capture("rhs"))
            ),
            (ruleMap, captures) -> (double)captures.get("lhs") + (int)captures.get("rhs")
        );
        rules.define(
            Patterns.conformsTo(
                Patterns.equalsObject("+"),
                Patterns.is(Integer.class).andThen(Patterns.capture("lhs")),
                Patterns.is(Double.class).andThen(Patterns.capture("rhs"))
            ),
            (ruleMap, captures) -> (int)captures.get("lhs") + (double)captures.get("rhs")
        );

        /*String src =
            "[\"aFunction\", String value] => [\"+\", value, \" was provided\"]?\n" +
            "[\"aFunction\", \"Argument\"]?";*/
        String src =
            "[\"toJava\", Integer value] => value\n" +
            "[\"toJava\", Double value] => value\n" +
            "[\"toJava\", [\"+\", Object lhs, Object rhs]] => (\"+\", (\"toJava\", lhs), (\"+\", \" + \", (\"toJava\", rhs)))\n" +
            "[\"toJava\", [\">\", Object lhs, Object rhs]] => (\"+\", (\"toJava\", lhs), (\"+\", \" > \", (\"toJava\", rhs)))\n" +
            "[\"toString\", Object obj] => (\"invoke\", (\"class\", \"java.lang.Object\"), obj, \"toString\", [], [])\n" +
            "[\"+\", String lhs, String rhs] => (\"invoke\", (\"class\", \"java.lang.String\"), lhs, \"concat\", [(\"class\", \"java.lang.String\")], [rhs])\n" +
            "[\"+\", String lhs, Object rhs] => (\"+\", lhs, (\"toString\", rhs))\n" +
            "[\"+\", Object lhs, String rhs] => (\"+\", (\"toString\", lhs), rhs)\n" +
            "(\"toJava\", [\">\", [\"+\", 3, 7], 5])";
            //"(\"+\", (\"toJava\", 5), (\"+\", \" > \", (\"toJava\", 1)))";
            //"[\"+\", \"Argument\", \"Another\"]?";
            //"[\"invoke\", [\"class\", \"java.lang.String\"]?, \"myString\", \"concat\", [[\"class\", \"java.lang.String\"]?], [\"otherString\"]]?";
        System.out.println(src);
        Object v = new midmod.pal.Evaluator(rules).evaluate(src);
        System.out.println(v);

        /*Object value = midmod.lisp.Parser.parse("(< (- (/ 5 3) 3) (* 2 56))");

        rules.define(
            Patterns.isInteger().andThen(Patterns.capture("v")),
            (ruleMap, captures) -> captures.get("v")
        );
        List<String> operators = Arrays.asList("+", "-", "*", "/", "<", ">", "<=", ">=");
        rules.define(
            Patterns.conformsTo(Arrays.asList(
                operators.stream().map(x -> Patterns.equalsObject(x)).reduce((x, y) -> x.or(y)).get().andThen(Patterns.capture("operator")),
                Patterns.capture("lhs"),
                Patterns.capture("rhs"))),
            (ruleMap, captures) -> Call.on(ruleMap, captures.get("lhs")) + " " + captures.get("operator") + " " + Call.on(ruleMap, captures.get("rhs"))
        );

        Object result = new Block(Arrays.asList(
            new Call(new Constant(value))
        )).perform(rules, new Hashtable<>());

        System.out.println(result);*/

        if(1 != 2)
            return;

        /*
        From reference models, project applications

        Could some sort of "algebraic collection oriented script language" be used?

        body => (
            is Binary

            ~.lhs | body
            ~ >> (
                is Add " + "
                | is Sub " - "
                | is Mul " * "
                | is Div " / "
                | is Rem " % "
            )
            ~.rhs | body
        )

        """public class $@.name {
            $(@.fields >>
                (^.modifier >> ~0 "public" | ~1 "private") " " @.type " " @.name
            )
            $(@.methods |
                (@.modifier >> ~0 "public" | ~1 "private") " " @.returnType " " @.name "(" ")" "{"
                    @.body >> body
                "}"
            )
        }"""

        */

        MapCell globals = new MapCell();

        /*JFrame frame = new JFrame();

        JTextArea console = new JTextArea();

        console.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String sourceCode = console.getText();
                    int start = console.getSelectionStart();
                    int end = console.getSelectionEnd();

                    if(start == end) {
                        start = 0;
                        end = console.getDocument().getLength();
                    }

                    try {
                        sourceCode = console.getDocument().getText(start, end - start);
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }

                    try {
                        Parser parser = new Parser(new ByteArrayInputStream(sourceCode.getBytes()));

                        Object result = parser.execute(globals);

                        console.getDocument().insertString(end, "\n=> " + result, null);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        frame.getContentPane().add(console);

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);*/

        new Parser(
            "Composite = {}\n" +
            "Composite.X = \"someValue\"\n" +
            "Name =  \"MyClass\"\n" +
            "FirstName =  \"John\"\n" +
            "LastName = \"Johnson\"\n"
        ).execute(globals);



        MapCell dict1 = new MapCell();
        MapCell dict1_1 = new MapCell();

        dict1.put("Composite", dict1_1);
        dict1_1.put("X", "someValue");

        MapCell dict2 = new MapCell();

        //dict1.addListener(dict2);

        dict1.put("Name", "MyClass");

        dict1.put("FirstName", "John");
        dict1.put("LastName", "Johnson");

        //dict1.addListener(Cells.get("Name").addListener(Cells.func((String x) -> x + "Extended").addListener(Cells.define("NameX").addListener(dict2))));

        dict1.addListener(Cells.get("Composite").addListener(Cells.get("X").addListener(Cells.put("CX").addListener(dict2))));

        FunctionMapCell environment = new FunctionMapCell();

        environment
            .reduce("concat",
                Arrays.asList(dict1.withListener(
                    Cells.get("FirstName")),
                    Cells.constant(" "),
                    dict1.withListener(Cells.get("LastName"))
                )
            ).addListener(Cells.put("FullName").addListener(dict2));

        Predicate<Object[]> allAreStrings = arguments -> Arrays.asList(arguments).stream().allMatch(x -> x instanceof String);
        environment.define("concat",
            allAreStrings,
            strings -> ((List<String>) (Object) Arrays.asList(strings)).stream().collect(Collectors.joining()));

        System.out.println("dict1:");
        System.out.println(dict1);
        System.out.println("dict2:");
        System.out.println(dict2);
        System.out.println("environment:");
        System.out.println(environment);

        dict1.remove("Name");

        environment.define("concat",
            allAreStrings,
            strings -> ((List<String>) (Object) Arrays.asList(strings)).stream().collect(Collectors.joining(":")));

        //((DictionaryCell)dict1.get("Composite")).define("X", "SomeNewValue");


        //dict1_1.define("X", "someValue2");

        //Cells.reduce(dict1.withListener(Cells.get("FirstName")), dict1.withListener(Cells.get("LastName")), String.class, String.class, (x, y) -> x + " " + y).addListener(Cells.define("FullName").addListener(dict2));

        dict1.put("Composite", dict1_1);

        //environment.define("concat", strings -> ((List<String>) (Object) Arrays.asList(strings)).stream().collect(Collectors.joining()));
        //environment.remove("concat");

        System.out.println("dict1:");
        System.out.println(dict1);
        System.out.println("dict2:");
        System.out.println(dict2);
        System.out.println("environment:");
        System.out.println(environment);
    }
}
