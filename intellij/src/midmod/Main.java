package midmod;

import midmod.json.Parser;
import midmod.pal.Evaluator;
import midmod.rules.Environment;
import midmod.rules.RuleMap;
import midmod.rules.actions.*;
import midmod.rules.actions.Action;
import midmod.rules.patterns.Pattern;
import midmod.rules.patterns.PatternFactory;
import midmod.rules.patterns.Patterns;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
        RuleMap rules = new RuleMap();

        RuleMap builtins = new RuleMap();

        RuleMap patternMappers = new RuleMap();

        patternMappers.define(
            Patterns.conformsTo(Patterns.equalsObject("equals"), Patterns.captureSingle(0, Patterns.anything)),
            (ruleMap, local, captures) ->
                Patterns.equalsObject(captures.get(0)));
        patternMappers.define(
            Patterns.conformsTo(
                Patterns.equalsObject("subsumes-list"),
                Patterns.conformsTo(Patterns.captureMany(0, Patterns.repeat(Patterns.anything)))
            ),
            (ruleMap, local, captures) ->
            {
                List<Object> items = (List<Object>) captures.get(0);
                return Patterns.conformsTo(items.stream().map(x -> (Pattern)Match.on(patternMappers, local, x)).collect(Collectors.toList()));
            });

        RuleMap actionMappers = new RuleMap();

        actionMappers.define(
            Patterns.conformsTo(
                Patterns.equalsObject("global-rules")
            ),
            (ruleMap, local, captures) -> new Action() {
                @Override
                public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
                    return ruleMap;
                }
            }
        );
        actionMappers.define(
            Patterns.conformsTo(
                Patterns.equalsObject("define"),
                Patterns.captureSingle(0, Patterns.anything),
                Patterns.captureSingle(1, Patterns.anything),
                Patterns.captureSingle(2, Patterns.anything)
            ),
            (ruleMap, local, captures) -> {
                List<Object> targetExpressionValue = (List<Object>) captures.get(0);
                List<Object> patternExpressionExpressionValue = (List<Object>) captures.get(1);
                List<Object> actionExpressionExpressionValue = (List<Object>) captures.get(2);

                Action target = (Action) Match.on(actionMappers, local, targetExpressionValue);
                Action patternExpressionExpression = (Action) Match.on(actionMappers, local, patternExpressionExpressionValue);
                Action actionExpressionExpression = (Action) Match.on(actionMappers, local, actionExpressionExpressionValue);

                return new Action() {
                    @Override
                    public Object perform(RuleMap ruleMap, RuleMap local, Environment captures) {
                        RuleMap rm = (RuleMap) target.perform(ruleMap, local, captures);
                        List<Object> patternExpressionValue = (List<Object>) patternExpressionExpression.perform(ruleMap, local, captures);
                        List<Object> actionExpressionsValue = (List<Object>) actionExpressionExpression.perform(ruleMap, local, captures);
                        Pattern pattern = (Pattern) Match.on(patternMappers, local, patternExpressionValue);
                        Action action = (Action) Match.on(actionMappers, local, actionExpressionsValue);
                        rm.define(pattern, action);

                        return rm;
                    }
                };
            }
        );

        actionMappers.define(
            Patterns.conformsTo(Patterns.equalsObject("constant"), Patterns.captureSingle(0, Patterns.anything)),
            (ruleMap, local, captures) ->
                new Constant(captures.get(0)));

        builtins.define(
            Patterns.conformsTo(
                Patterns.equalsObject("new-rule-map"),
                Patterns.conformsTo(
                    Patterns.captureMany(0, Patterns.repeat(Patterns.conformsTo(
                        Patterns.anything,
                        Patterns.anything
                    )))
                )
            ),
            (ruleMap, local, captures) -> {
                List<Object> patternActions = (List<Object>) captures.get(0);
                RuleMap rm = new RuleMap();
                patternActions.forEach(x -> {
                    Pattern pattern = (Pattern) Match.on(patternMappers, local, ((List<Object>)x).get(0));
                    Action action = (Action) Match.on(actionMappers, local, ((List<Object>)x).get(1));
                    rm.define(pattern, action);
                });
                return rm;
            }
        );
        /*builtins.define(
            Patterns.conformsTo(
                Patterns.equalsObject("global-rules")
            ),
            (ruleMap, local, captures) -> ruleMap
        );
        builtins.define(
            Patterns.conformsTo(
                Patterns.equalsObject("define"),
                Patterns.captureSingle(0, Patterns.anything),
                Patterns.captureSingle(1, Patterns.anything),
                Patterns.captureSingle(2, Patterns.anything)
            ),
            (ruleMap, local, captures) -> {
                RuleMap rm = (RuleMap) captures.get(0);
                List<Object> patternExpression = (List<Object>) captures.get(1);
                List<Object> actionExpression = (List<Object>) captures.get(2);
                Pattern pattern = (Pattern) Match.on(patternMappers, local, patternExpression);
                Action action = (Action) Match.on(actionMappers, local, actionExpression);
                rm.define(pattern, action);
                return action;
            }
        );*/

        //PatternFactory.newRuleMap(PatternFactory.equalsObject("myString"), ActionFactory.constant("x"));

        /*Match.on(builtins, builtins, PatternFactory.newRuleMap(
            PatternFactory.rule(PatternFactory.equalsObject("myString"), ActionFactory.constant("x")))
        );*/
        /*Match.on(builtins, builtins, PatternFactory.newRuleMap(
            PatternFactory.rule(
                PatternFactory.subsumesList(
                    PatternFactory.equalsObject("str1"),
                    PatternFactory.equalsObject("str2")
                ),
                ActionFactory.constant("x"))
            )
        );*/
        Action action = (Action) Match.on(actionMappers, actionMappers, ActionFactory.define(
            ActionFactory.globalRules(),
            ActionFactory.constant(PatternFactory.subsumesList(
                PatternFactory.equalsObject("str1"),
                PatternFactory.equalsObject("str2")
            )),
            ActionFactory.constant(ActionFactory.constant("x"))
        ));
        action.perform(actionMappers, actionMappers, new Environment());

        boolean addBuiltins = true;

        if(addBuiltins) {
            rules.define(
                Patterns.conformsTo(
                    Patterns.equalsObject("+"),
                    Patterns.captureSingle(0, Patterns.is(String.class)),
                    Patterns.captureSingle(1, Patterns.is(String.class))
                ),
                (ruleMap, local, captures) -> (String) captures.get(0) + (String) captures.get(1)
            );

            rules.define(
                Patterns.conformsTo(
                    Patterns.equalsObject("toNative"),
                    Patterns.captureSingle(0, Patterns.is(String.class))
                ),
                (ruleMap, local, captures) -> toNative(captures.get(0))
            );

            rules.define(
                Patterns.conformsTo(
                    Patterns.equalsObject("class"),
                    Patterns.captureSingle(0, Patterns.is(String.class))
                ),
                (ruleMap, local, captures) -> {
                    try {
                        return Class.forName((String) captures.get(0));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    return null; // Should be an error; how to handle errors? First class frames? Special case matching?
                }
            );

            rules.define(
                Patterns.conformsTo(
                    Patterns.equalsObject("match"),
                    Patterns.captureSingle(0, Patterns.anything),
                    Patterns.captureSingle(1, Patterns.is(RuleMap.class))
                ),
                (ruleMap, local, captures) -> {
                    Object value = captures.get(0);
                    RuleMap rulesToUse = (RuleMap) captures.get(1);

                    Object result = Call.onLocal(rules, rulesToUse, value);

                    return result;
                }
            );

            rules.define(
                Patterns.conformsTo(
                    Patterns.equalsObject("map"),
                    Patterns.captureSingle(0, Patterns.is(List.class)),
                    Patterns.captureSingle(1, Patterns.is(RuleMap.class))
                ),
                (ruleMap, local, captures) -> {
                    List<Object> list = (List<Object>) captures.get(0);
                    RuleMap rulesToUse = (RuleMap) captures.get(1);

                    return list.stream().map(x ->
                        {
                            Object result = Call.onLocal(rules, rulesToUse, x);
                            return result;
                        }).collect(Collectors.toList());
                    /*Object result = Call.on(rulesToUse, value);

                    return result;*/
                }
            );

            rules.define(
                Patterns.conformsTo(
                    Patterns.equalsObject("invoke"),
                    Patterns.captureSingle(0, Patterns.is(Class.class)),
                    Patterns.captureSingle(1, Patterns.is(Object.class)),
                    Patterns.captureSingle(2, Patterns.is(String.class)),
                    Patterns.captureSingle(3, Patterns.is(List.class)),
                    Patterns.captureSingle(4, Patterns.is(List.class))
                    ),
                (ruleMap, local, captures) -> {
                    try {
                        Class<?> klass = (Class<?>)captures.get(0);
                        Object instance = captures.get(1);
                        String methodName = (String)captures.get(2);
                        List<Class<?>> parameterTypes = (List<Class<?>>)captures.get(3);
                        List<Object> arguments = (List<Object>)captures.get(4);

                        Class[] parameterTypesAsArray = parameterTypes.stream().toArray(s -> new Class[s]);
                        Method method = klass.getMethod(methodName, parameterTypesAsArray);
                        Object[] argumentsAsArray = (arguments).stream().toArray(s -> new Object[s]);
                        return method.invoke(instance, argumentsAsArray);
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

            rules.defineBinary("+", Double.class, Double.class, (rhs, lhs) -> rhs + lhs);
            rules.defineBinary("+", Double.class, Integer.class, (rhs, lhs) -> rhs + lhs);
            rules.defineBinary("+", Integer.class, Double.class, (rhs, lhs) -> rhs + lhs);
            rules.defineBinary("+", Integer.class, Integer.class, (rhs, lhs) -> rhs + lhs);
            rules.defineBinary("-", Double.class, Double.class, (rhs, lhs) -> rhs - lhs);
            rules.defineBinary("-", Double.class, Integer.class, (rhs, lhs) -> rhs - lhs);
            rules.defineBinary("-", Integer.class, Double.class, (rhs, lhs) -> rhs - lhs);
            rules.defineBinary("-", Integer.class, Integer.class, (rhs, lhs) -> rhs - lhs);
            rules.defineBinary("*", Double.class, Double.class, (rhs, lhs) -> rhs * lhs);
            rules.defineBinary("*", Double.class, Integer.class, (rhs, lhs) -> rhs * lhs);
            rules.defineBinary("*", Integer.class, Double.class, (rhs, lhs) -> rhs * lhs);
            rules.defineBinary("*", Integer.class, Integer.class, (rhs, lhs) -> rhs * lhs);
            rules.defineBinary("/", Double.class, Double.class, (rhs, lhs) -> rhs / lhs);
            rules.defineBinary("/", Double.class, Integer.class, (rhs, lhs) -> rhs / lhs);
            rules.defineBinary("/", Integer.class, Double.class, (rhs, lhs) -> rhs / lhs);
            rules.defineBinary("/", Integer.class, Integer.class, (rhs, lhs) -> rhs / lhs);
        }

        new Thread(() -> {
            // Just some dummy source code for "warming up" the evaluator and parser
            String src = "{operator = \"+\", arg = \"MyString\"}";
            try {
                new Evaluator(new RuleMap()).evaluate(src);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).run();
        Evaluator evaluator = new Evaluator(rules);

        JFrame frame = new JFrame();

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
                        //Object result = evaluator.evaluate(new ByteArrayInputStream(sourceCode.getBytes()));
                        Object result = evaluator.evaluateAsValue(new ByteArrayInputStream(sourceCode.getBytes()));

                        String outputText = " " + result;
                        console.getDocument().insertString(end, outputText, null);
                        console.select(end + 1, end + outputText.length());
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
        frame.setVisible(true);

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

        //dict1.addListener(Cells.getIndexFor("Name").addListener(Cells.func((String x) -> x + "Extended").addListener(Cells.define("NameX").addListener(dict2))));

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

        //((DictionaryCell)dict1.getIndexFor("Composite")).define("X", "SomeNewValue");


        //dict1_1.define("X", "someValue2");

        //Cells.reduce(dict1.withListener(Cells.getIndexFor("FirstName")), dict1.withListener(Cells.getIndexFor("LastName")), String.class, String.class, (x, y) -> x + " " + y).addListener(Cells.define("FullName").addListener(dict2));

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
