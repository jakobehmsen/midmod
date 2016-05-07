package reo;

import com.sun.glass.events.KeyEvent;
import reo.lang.Parser;
import reo.runtime.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class Main {
    private static void attachDecorator(JComponent workspace, JComponent componentToDecorate, Runnable removeAction) {
        componentToDecorate.addMouseListener(new MouseAdapter() {
            boolean mouseDown;
            Timer timer;
            Runnable onHide;

            @Override
            public void mouseEntered(MouseEvent e) {
                timer = new Timer(1000, e2 -> {
                    synchronized (this) {
                        JComponent decorator = new JPanel(new BorderLayout());
                        decorator.setBackground(Color.DARK_GRAY);
                        decorator.setBorder(BorderFactory.createRaisedBevelBorder());
                        JToolBar toolBar = new JToolBar();
                        toolBar.setFloatable(false);
                        toolBar.add(new AbstractAction("Del") {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                removeAction.run();
                            }
                        });
                        decorator.add(toolBar, BorderLayout.SOUTH);
                        int paddingH = 0;
                        int paddingTop = 0;
                        int paddingBottom = 30;
                        int paddingV = paddingTop + paddingBottom;
                        decorator.setSize(new Dimension(
                            paddingH + componentToDecorate.getWidth() + paddingH,
                            paddingTop + componentToDecorate.getHeight() + paddingBottom
                        ));
                        decorator.setBackground(new Color(0,0,0,0));
                        decorator.setLocation(new Point(
                            componentToDecorate.getX() - paddingH,
                            componentToDecorate.getY() - paddingTop
                        ));
                        decorator.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createDashedBorder(Color.BLACK),
                            BorderFactory.createDashedBorder(Color.WHITE)
                        ));
                        workspace.add(decorator);
                        workspace.setComponentZOrder(decorator, workspace.getComponentZOrder(componentToDecorate));
                        workspace.repaint();
                        workspace.revalidate();

                        JButton removeButton = new JButton("X");
                        addMouseExitHandler(removeButton, decorator);

                        ContainerAdapter removeListener = new ContainerAdapter() {
                            @Override
                            public void componentRemoved(ContainerEvent e) {
                                if(e.getChild() == componentToDecorate) {
                                    hide(decorator);
                                }
                            }
                        };

                        Container componentToDecorateParent = componentToDecorate.getParent();
                        onHide = () -> {
                            componentToDecorateParent.removeContainerListener(removeListener);
                        };

                        componentToDecorate.getParent().addContainerListener(removeListener);

                        MouseAdapter mouseAdapter = new MouseAdapter(){
                            int mouseDownX;
                            int mouseDownY;

                            @Override
                            public void mousePressed(MouseEvent e) {
                                mouseDownX = e.getX();
                                mouseDownY = e.getY();
                                mouseDown = true;
                            }

                            public void mouseDragged(MouseEvent e)
                            {
                                int x = e.getX() + decorator.getX() - mouseDownX;
                                int y = e.getY() + decorator.getY() - mouseDownY;
                                decorator.setLocation(x, y);
                                componentToDecorate.setLocation(paddingH + x, paddingTop + y);
                            }

                            @Override
                            public void mouseReleased(MouseEvent e) {
                                mouseDown = false;
                            }
                        };

                        decorator.addMouseListener(mouseAdapter);
                        decorator.addMouseMotionListener(mouseAdapter);

                        addMouseExitHandler(decorator, decorator);

                        timer = null;
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }

            private void addMouseExitHandler(JComponent component, JComponent decorator) {
                component.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if(mouseDown)
                            return;
                        // Invoked when hoovering over remove button

                        java.awt.Point p = new java.awt.Point(e.getLocationOnScreen());
                        SwingUtilities.convertPointFromScreen(p, decorator);
                        if(decorator.contains(p)) {
                            return;
                        }

                        hide(decorator);
                    }
                });
            }

            private void hide(JComponent decorator) {
                onHide.run();

                workspace.remove(decorator);
                workspace.repaint();
                workspace.revalidate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                java.awt.Point p = new java.awt.Point(e.getLocationOnScreen());
                SwingUtilities.convertPointFromScreen(p, componentToDecorate);
                if(componentToDecorate.contains(p)) {
                    return;
                }

                synchronized (this) {
                    if (timer != null) {
                        timer.stop();
                        timer = null;
                    }
                }
            }
        });
    }

    public static void main(String[] args) throws NoSuchMethodException {
        Dictionary d = new Dictionary(null);

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

        Universe universe = new Universe(d);

        d.put("panel/0", Observables.constant(new Method(universe, new Behavior(new Instruction[] {
            Instructions.load(0),
            Instructions.wrapComponent(JPanel.class),
            Instructions.ret()
        }, 1, 1))));
        //universe.getIntegerPrototype().put("+/1", Observables.constant((Invokable) (s, a) ->
        //     (int) s + (int) a[0]));
        universe.getIntegerPrototype().put("+/1", Observables.constant(new Method(universe, new Behavior(new Instruction[] {
            Instructions.load(0),
            Instructions.load(1),
            Instructions.addi(),
            Instructions.ret()
        }, 1, 1))));
        /*universe.getIntegerPrototype().put("+/1", Observables.constant(new ReducerConstructor() {
            @Override
            public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(new Constant(self), arguments[0]), a ->
                    (int) a[0] + (int) a[1]);
            }
        }));*/
        universe.getStringPrototype().put("+/1", Observables.constant(new Method(universe, new Behavior(new Instruction[] {
            Instructions.load(0),
            Instructions.load(1),
            Instructions.javaInvokeInstance(String.class.getMethod("concat", new Class<?>[]{String.class})),
            Instructions.wrapComponent(JPanel.class),
            Instructions.ret()
        }, 1, 1))));
        /*universe.getStringPrototype().put("+/1", Observables.constant(new ReducerConstructor() {
            @Override
            public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                return new Reducer(Arrays.asList(new Constant(self), arguments[0]), a ->
                    (String) a[0] + (String) a[1]);
            }
        }));*/
        // How to associate the slots method to dictionaries?
        // Have a meta object, that handles creation of dictionaries?
        // How can delta object inheriance be supported?
        // - deltaObj = delta || prototype // where || means creates a composite dictionary, that is a projection of delta and prototype
        // but then || cannot be send to delta, but should be sent to prototype
        // deltaObj = prototype.delta(delta) // This way, delta can be sent to prototype
        /*universe.getDictPrototype().put("slots", Observables.constant(new ReducerConstructor() {
            @Override
            public Observable create(Object self, Dictionary prototype, Observable[] arguments) {
                return ((Dictionary)self).slots();
            }
        }));*/

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
                toolBar.add(new AbstractAction("Put") {
                    @Override
                    public void actionPerformed(ActionEvent e2) {
                        Observable result = eval(universe, textArea, d);

                        JLabel representation = new JLabel();

                        representation.setSize(((ComponentUI) representation.getUI()).getPreferredSize(representation));
                        representation.setLocation(e.getPoint());
                        representation.setToolTipText(textArea.getText());
                        //representation.setSize(100, 30);
                        workspace.add(representation);

                        Binding binding = result.bind(new Observer() {
                            Object theValue;
                            Object theError;

                            Color b = representation.getForeground();

                            {
                                error("Uninitialized");
                            }

                            @Override
                            public void initialize() {
                                representation.setForeground(b);
                            }

                            @Override
                            public void handle(Object value) {
                                if(theValue == null && value != null) {
                                    representation.setForeground(b);
                                    setText(value.toString());
                                } else if(theValue != null && value == null)
                                    release();
                            }

                            @Override
                            public void error(Object error) {
                                theError = error;
                                setText(error.toString());
                                representation.setForeground(Color.RED);
                            }

                            private void setText(String text) {
                                representation.setText(text);
                                representation.setSize(((ComponentUI) representation.getUI()).getPreferredSize(representation));
                                representation.revalidate();
                                representation.repaint();
                            }

                            @Override
                            public void release() {
                                error("Uninitialized");
                            }
                        });

                        attachDecorator(workspace, representation, () -> {
                            binding.remove();

                            workspace.remove(representation);
                            workspace.repaint();
                            workspace.revalidate();
                        });

                        workspace.remove(creation);
                        workspace.repaint();
                        workspace.revalidate();
                    }
                });
                toolBar.add(new AbstractAction("Put2") {
                    @Override
                    public void actionPerformed(ActionEvent e2) {
                        Observable result = eval(universe, textArea, d);

                        //representation.setSize(((ComponentUI) representation.getUI()).getPreferredSize(representation));

                        Binding binding = result.bind(new Observer() {
                            Object theValue;
                            Object theError;

                            //Color b = representation.getForeground();

                            {
                                error("Uninitialized");
                            }

                            @Override
                            public void initialize() {
                                //representation.setForeground(b);
                            }

                            private ComponentDictionary cd;
                            private JComponent representation;

                            @Override
                            public void handle(Object value) {
                                // Value may be an update to the current value

                                if(value instanceof ComponentDictionary) {
                                    cd = ((ComponentDictionary)value);
                                    representation = cd.getComponent();
                                    cd.get("background").addObserver(new Observer() {
                                        @Override
                                        public void handle(Object value) {

                                        }
                                    });
                                    representation.setBackground(Color.BLUE);
                                    representation.setSize(100, 30);
                                    representation.setLocation(e.getPoint());
                                    representation.setToolTipText(textArea.getText());
                                    workspace.add(representation);
                                    representation.revalidate();
                                    representation.repaint();

                                    attachDecorator(workspace, representation, () -> {
                                        //binding.remove();

                                        workspace.remove(representation);
                                        workspace.repaint();
                                        workspace.revalidate();
                                    });
                                }

                                if(theValue == null && value != null) {
                                    //representation.setForeground(b);
                                    //setText(value.toString());
                                } else if(theValue != null && value == null)
                                    release();
                            }

                            @Override
                            public void error(Object error) {
                                theError = error;
                                setText(error.toString());
                                //representation.setForeground(Color.RED);
                            }

                            private void setText(String text) {
                                /*representation.setText(text);
                                representation.setSize(((ComponentUI) representation.getUI()).getPreferredSize(representation));
                                representation.revalidate();
                                representation.repaint();*/
                            }

                            @Override
                            public void release() {
                                error("Uninitialized");
                            }
                        });

                        workspace.remove(creation);
                        workspace.repaint();
                        workspace.revalidate();
                    }
                });
                toolBar.add(new AbstractAction("Text") {
                    @Override
                    public void actionPerformed(ActionEvent e2) {
                        Observable result = eval(universe, textArea, d);

                        Getter getter = result.toGetter();

                        JTextField getterView = new JTextField();

                        getter.toView(new AbstractViewAdapter() {
                            Object value = getterView.getText();

                            {
                                getterView.getDocument().addDocumentListener(new DocumentListener() {
                                    @Override
                                    public void insertUpdate(DocumentEvent e) {
                                        value = getterView.getText();
                                        sendChange(value);
                                    }

                                    @Override
                                    public void removeUpdate(DocumentEvent e) {
                                        value = getterView.getText();
                                        sendChange(value);
                                    }

                                    @Override
                                    public void changedUpdate(DocumentEvent e) {

                                    }
                                });
                            }

                            @Override
                            protected void sendStateTo(Observer observer) {
                                observer.handle(value);
                            }

                            @Override
                            public void initialize(Object value) {
                                getterView.setText(value.toString());
                            }
                        });

                        attachDecorator(workspace, getterView, () -> {
                            getter.remove();
                            workspace.remove(getterView);
                            workspace.repaint();
                            workspace.revalidate();
                        });

                        getterView.setLocation(e.getPoint());
                        getterView.setSize(200, 30);
                        getterView.setToolTipText(textArea.getText());
                        workspace.add(getterView);

                        workspace.remove(creation);
                        workspace.repaint();
                        workspace.revalidate();
                    }
                });
                toolBar.add(new AbstractAction("Number") {
                    @Override
                    public void actionPerformed(ActionEvent e2) {
                        Observable result = eval(universe, textArea, d);

                        Getter getter = result.toGetter();

                        JSpinner getterView = new JSpinner();

                        getter.toView(new AbstractViewAdapter() {
                            Object value = getterView.getValue();

                            {
                                getterView.addChangeListener(new ChangeListener() {
                                    @Override
                                    public void stateChanged(ChangeEvent e) {
                                        value = getterView.getValue();
                                        sendChange(value);
                                    }
                                });
                            }

                            @Override
                            protected void sendStateTo(Observer observer) {
                                observer.handle(value);
                            }

                            @Override
                            public void initialize(Object value) {
                                getterView.setValue(value);
                            }
                        });

                        attachDecorator(workspace, getterView, () -> {
                            getter.remove();
                            workspace.remove(getterView);
                            workspace.repaint();
                            workspace.revalidate();
                        });

                        getterView.setLocation(e.getPoint());
                        getterView.setSize(200, 30);
                        getterView.setToolTipText(textArea.getText());
                        workspace.add(getterView);

                        workspace.remove(creation);
                        workspace.repaint();
                        workspace.revalidate();
                    }
                });
                toolBar.add(new AbstractAction("Message") {
                    @Override
                    public void actionPerformed(ActionEvent e2) {
                        JLabel view = new JLabel(textArea.getText());

                        view.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 12));

                        view.setLocation(e.getPoint());
                        view.setSize(((ComponentUI) view.getUI()).getPreferredSize(view));

                        attachDecorator(workspace, view, () -> {
                            workspace.remove(view);
                            workspace.repaint();
                            workspace.revalidate();
                        });
                        workspace.add(view);

                        workspace.remove(creation);
                        workspace.repaint();
                        workspace.revalidate();

                        /*eval(universe, textArea, d);

                        workspace.remove(creation);
                        workspace.repaint();
                        workspace.revalidate();*/

                        /*
                        Create and put a physical element that can be dragged and dropped upon another object, to which
                        the message is sent.
                        The constitutes all of the script, where the dropee is the "this" context.

                        So, a message is a composite "thing" here; it's a parameterized script with "this" being the
                        parameter.
                        Thus, another name than "Message" should be considered.
                        */
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
                creationPanel.setSize(300, 100);
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
            Instructions.newDeltaObject(),
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
