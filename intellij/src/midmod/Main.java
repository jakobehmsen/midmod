package midmod;

import midmod.json.Parser;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
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

        //dict1.addListener(Cells.get("Name").addListener(Cells.func((String x) -> x + "Extended").addListener(Cells.put("NameX").addListener(dict2))));

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

        //((DictionaryCell)dict1.get("Composite")).put("X", "SomeNewValue");


        //dict1_1.put("X", "someValue2");

        //Cells.reduce(dict1.withListener(Cells.get("FirstName")), dict1.withListener(Cells.get("LastName")), String.class, String.class, (x, y) -> x + " " + y).addListener(Cells.put("FullName").addListener(dict2));

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
