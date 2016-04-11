package reo;

import com.sun.glass.events.KeyEvent;
import reo.lang.Parser;
import reo.runtime.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Dictionary d = new Dictionary();

        /*d.put("asdf", new Constant("sdf"));

        d.put("x", new Constant(2));
        d.put("y", new Constant(4));

        new Reducer(Arrays.asList(d.get("x"), d.get("y")), a -> (int)a[0] + (int)a[1])
            .addObserver(value -> System.out.println("Reduced to: " + value));

        d.put("x", new Constant(7));

        d.put("method", new Constant(new ReducerConstructor() {
            @Override
            public Reducer create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(prototype.get("x"), prototype.get("y"), arguments[0]), a ->
                    (int)a[0] * (int)a[1] * (int)a[2]);
            }
        }));

        d.apply(d, "method", new Observable[]{new Constant(10)})
            .addObserver(new Observer() {
                @Override
                public void handle(Object value) {
                    System.out.println("method reduced to: " + value);
                }

                @Override
                public void release() {
                    System.out.println("method application was released");
                }
            });

        d.put("x", new Constant(8));

        d.put("method", new Constant(new ReducerConstructor() {
            @Override
            public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(prototype.get("y"), arguments[0]), a ->
                    (int)a[0] * (int)a[1]);
            }
        }));

        d.put("xTimesY", new Reducer(Arrays.asList(d.get("x"), d.get("y")), a -> (int)a[0] * (int)a[1]));
        d.get("xTimesY").addObserver(new Observer() {
            @Override
            public void handle(Object value) {
                System.out.println("xTimesY reduced to: " + value);
            }

            @Override
            public void release() {
                System.out.println("xTimesY was released");
            }
        });

        d.put("x", new Constant(9));

        d.remove("x");*/

        Universe universe = new Universe();
        universe.getIntegerPrototype2().put("+", Observables.constant(new ReducerConstructor() {
            @Override
            public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(new Constant(self), arguments[0]), a ->
                    (int) a[0] + (int) a[1]);
            }
        }));
        universe.getIntegerPrototype2().put("+/1", Observables.constant(new ReducerConstructor() {
            @Override
            public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(new Constant(self), arguments[0]), a ->
                    (int) a[0] + (int) a[1]);
            }
        }));

        /*
        - A first language
        */

        JFrame playground = new JFrame("Reo playground");

        JPanel workspace = new JPanel();
        workspace.setLayout(null);

        playground.setLayout(new BorderLayout());
        playground.getContentPane().add(workspace, BorderLayout.CENTER);

        workspace.addMouseListener(new MouseAdapter() {
            JComponent creation;

            @Override
            public void mouseClicked(MouseEvent e) {
                if(creation != null) {
                    workspace.remove(creation);
                    creation = null;
                }

                JPanel creationPanel = new JPanel(new BorderLayout());

                JTextArea textArea = new JTextArea();

                JToolBar toolBar = new JToolBar();
                toolBar.setFloatable(false);
                toolBar.add(new AbstractAction("Eval") {
                    @Override
                    public void actionPerformed(ActionEvent e2) {
                        Observable result = eval(universe, textArea, d);

                        JLabel representation = new JLabel();

                        representation.setSize(((ComponentUI) representation.getUI()).getPreferredSize(representation));
                        representation.setLocation(e.getPoint());
                        //representation.setSize(100, 30);
                        workspace.add(representation);

                        result.addObserver(new Observer() {
                            @Override
                            public void handle(Object value) {
                                representation.setText(value.toString());
                                representation.setSize(((ComponentUI) representation.getUI()).getPreferredSize(representation));
                                representation.revalidate();
                                representation.repaint();
                            }

                            @Override
                            public void release() {
                                workspace.remove(representation);
                            }
                        });

                        workspace.remove(creation);
                        workspace.repaint();
                        workspace.revalidate();
                    }
                });
                toolBar.add(new AbstractAction("Do") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        eval(universe, textArea, d);

                        workspace.remove(creation);
                        workspace.repaint();
                        workspace.revalidate();
                    }
                });

                textArea.registerKeyboardAction(e1 -> {
                    workspace.remove(creation);
                    workspace.repaint();
                    workspace.revalidate();
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_FOCUSED);

                //textArea.setLocation(e.getPoint());

                //textArea.setSize(10, 20);
                //textArea.setOpaque(false);

                creationPanel.add(textArea, BorderLayout.CENTER);
                creationPanel.add(toolBar, BorderLayout.SOUTH);
                creationPanel.setSize(200, 100);
                creationPanel.setLocation(e.getPoint());

                creation = creationPanel;

                workspace.add(creation);
                workspace.setComponentZOrder(creation, 0);

                workspace.revalidate();
                workspace.repaint();
                textArea.requestFocusInWindow();
            }
        });

        playground.setSize(1024, 768);
        playground.setLocationRelativeTo(null);
        playground.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playground.setVisible(true);

        /*String script =
            "test = 1\n" +
            "asdf = y + 1\n" +
            "objectL = #{y = test}\n" +
            "test = 12\n" +
            "someMethod(arg) => test + arg\n" +
            "test2 = this.someMethod(5)\n" +
            "";
        Behavior behavior = Parser.parse(script);
        Evaluation evaluation = new Evaluation(universe, behavior.createFrame(null, d, new Observable[0]));
        evaluation.evaluate();

        System.out.println(d);*/

        /*Frame frame = new Frame(null, new Instruction[] {
            Instructions.load(0),
            Instructions.newDict(),
            Instructions.storeSlot("obj"),

            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadConstant(10),
            Instructions.storeSlot("i"),

            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadConstant(11),
            Instructions.storeSlot("m"),

            Instructions.load(0),
            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadSlot("i"),
            Instructions.storeSlot("j"),

            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadConstant(15),
            Instructions.storeSlot("i"),

            Instructions.load(0),
            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadSlot("i"),
            Instructions.load(0),
            Instructions.loadSlot("obj"),
            Instructions.loadSlot("m"),
            Instructions.messageSend("+", 1),
            Instructions.storeSlot("someSum"),



            Instructions.load(0),
            Instructions.loadConstant(8),
            Instructions.storeSlot("x"),

            Instructions.load(0),
            Instructions.loadConstant(18),
            Instructions.storeSlot("y"),

            Instructions.load(0),

            Instructions.load(0),
            Instructions.loadSlot("x"),
            Instructions.load(0),
            Instructions.loadSlot("y"),
            Instructions.addi(),

            Instructions.storeSlot("sum"),

            Instructions.load(0),
            Instructions.loadConstant(10),
            Instructions.storeSlot("y"),

            Instructions.load(0),
            Instructions.removeSlot("y"),

            //Instructions.loadConstant("Finished"),
            Instructions.halt()
        });
        Dictionary self = new Dictionary();

        self.addObserver(new ReflectiveObserver() {
            void handle(Dictionary.PutSlotChange putSlotChange) {
                System.out.println("Allocated slot " + putSlotChange.getName());

                putSlotChange.getSlot().addObserver(new Observer() {
                    @Override
                    public void handle(Object value) {
                        System.out.println("Set slot " + putSlotChange.getName() + " to " + value);
                    }

                    @Override
                    public void release() {
                        System.out.println("Deallocated slot " + putSlotChange.getName());
                    }
                });
            }
        });

        frame.push(new Constant(self));
        Evaluation evaluation = new Evaluation(universe, frame);
        evaluation.evaluate();
        Observable result = evaluation.getFrame().pop();
        System.out.println(result);*/
    }

    private static Observable eval(Universe universe, JTextComponent script, Object self) {
        String text = script.getSelectedText();
        if (text == null)
            text = script.getText();

        Behavior behavior = Parser.parse(text);
        Evaluation evaluation = new Evaluation(universe, behavior.createFrame(null, self, new Observable[0]));
        evaluation.evaluate();

        return evaluation.getFrame().pop();
    }
}
