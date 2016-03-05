package jsflow;

import com.sun.glass.events.KeyEvent;
import jdk.nashorn.api.scripting.*;
import jdk.nashorn.internal.runtime.Undefined;
import jsflow.bindlang.Parser;
import midmod.parse.*;

import javax.script.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    private static class JSComponent extends JComponent {
        private ObservableJSObject object;

        private JSComponent(ObservableJSObject object) {
            this.object = object;
        }

        @Override
        public void paint(Graphics g) {
            ScriptObjectMirror paintFunction = (ScriptObjectMirror) object.getMember("paint");
            paintFunction.call(object, g);
            //object.callMember("paint", g);
            //ScriptObjectMirror paintFunction = (ScriptObjectMirror) object.get("paint");

            //g.setColor(Color.RED);
            //paintFunction.call(object, g);

            //g.fillRect(0, 0, getWidth(), getHeight());
        }

        /*@Override
        public Color getBackground() {
            return (Color)object.getMember("background");
        }*/

        /*@Override
        public Point getLocation() {
            return new Point(getX(), getY());
        }

        @Override
        public Dimension getSize(Dimension rv) {
            return new Dimension(getWidth(), getHeight());
        }

        @Override
        public int getX() {
            return (int)object.getMember("x");
        }

        @Override
        public int getY() {
            return (int)object.getMember("y");
        }

        @Override
        public int getWidth() {
            return (int)object.getMember("width");
        }

        @Override
        public int getHeight() {
            return (int)object.getMember("height");
        }*/

        /*@Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            object.setMember("background", bg);
        }

        @Override
        public void setLocation(int x, int y) {
            super.setLocation(x, y);
            object.setMember("x", x);
            object.setMember("y", y);
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);
            object.setMember("width", width);
            object.setMember("height", height);
        }*/
    }

    public interface Removable {
        void remove();
    }

    public interface Binding extends Removable {
        Object getSource();
        Object getTarget();
    }

    public interface Observable {
        void addObserver(Object observer);
        default <T> T addAndReturnObserver(T observer) {
            addObserver(observer);
            return (T)observer;
        }
        void removeObserver(Object observer);
        void sendState();
        void getState(Consumer<Object> valueHandler);
    }

    public interface Observer extends Removable {
        void next(Object value);
    }

    public abstract static class AbstractObservable implements Observable, Removable {
        private ArrayList<Object> observers = new ArrayList<>();

        @Override
        public void addObserver(Object observer) {
            observers.add(observer);
        }

        @Override
        public void removeObserver(Object observer) {
            observers.remove(observer);
            ((Observer) observer).remove();
        }

        protected void sendNext(Object value) {
            observers.forEach(x -> ((Observer) x).next(value));
            //observers.forEach(x -> x.call("next", value));
        }

        protected void sendRemove() {
            observers.forEach(x -> ((Observer) x).remove());
            //observers.forEach(x -> x.call("remove"));
        }

        @Override
        public void remove() {
            sendRemove();
        }

        @Override
        public void sendState() {
            getState(value -> sendNext(value));
        }
    }

    public abstract static class AbstractObservableObserver extends AbstractObservable implements Observer {

    }

    private static Point mouseClickPoint;
    public static class Facade {
        private NashornScriptEngine engine;
        private JPanel desktop;
        private ArrayList<Binding> bindings = new ArrayList<>();
        private ArrayList<ObservableJSObject> allObjects = new ArrayList<>();
        private Hashtable<String, ObservableJSObject> indexedObjects = new Hashtable<>();
        private ObservableJSObject base;

        private Facade(NashornScriptEngine engine, JPanel desktop, ObservableJSObject base) {
            this.engine = engine;
            this.desktop = desktop;
            this.base = base;
        }

        public ObservableJSObject clone(Object obj) {
            return new ObservableJSObject((ObservableJSObject) obj);

            /*JSComponent cellComponent = createCell(engine, desktop, new ObservableJSObject((ObservableJSObject) obj), indexedObjects, base);

            cellComponent.object.sendState();

            desktop.add(cellComponent);
            desktop.repaint();
            desktop.revalidate();

            return cellComponent.object;*/
        }

        public ObservableJSObject getByName(String name) {
            return indexedObjects.get(name);
        }

        //public Observable memberSource(JSObject obj, String member)
        public Observable memberSource(Observable observable, String member) {
            return new AbstractObservable() {
                Consumer<Map<String, Object>> observer;
                Object obj;

                {
                    observable.addObserver(new Observer() {
                        @Override
                        public void next(Object value) {
                            // From:
                            // #{ function(g) {
                            //     g.drawString(this.text, 0, this.height);
                            // } }#
                            // for:
                            // core.dependent(core.either([core.either([core.memberSource(core.constSource(this), "text"), core.constSource(0), core.memberSource(core.constSource(this), "height"), core.memberSource(core.constSource("DUMMY DOESN'T CHANGE"), "drawString")])]), core.constSource(function(g){g.drawString(this.text,0,this.height)}))...
                            // this fails due to:
                            // core.memberSource(core.constSource("DUMMY DOESN'T CHANGE"), "drawString")
                            // because value is a String and cannot be cast to a JSObject
                            // "DUMMY DOESN'T CHANGE" is inserted because of the access to g where g is a parameter
                            if(obj instanceof ObservableJSObject) {
                                ((ObservableJSObject) obj).removeObserver(observer);
                            }

                            obj = value;

                            if(obj instanceof ObservableJSObject) {
                                observer = change -> {
                                    switch ((String) change.get("type")) {
                                        case "put":
                                        case "update": {
                                            String name = (String) change.get("name");
                                            if (name.equals(member)) {
                                                Object v = change.get("value");
                                                sendNext(v);
                                            }
                                            break;
                                        }
                                        case "remove": {
                                            String name = (String) change.get("name");
                                            if (name.equals(member)) {
                                                Object v = Undefined.getUndefined();
                                                sendNext(v);
                                            }
                                            break;
                                        }
                                    }
                                };

                                ((ObservableJSObject) obj).addObserver(observer);
                            }

                            sendState();
                        }

                        @Override
                        public void remove() {
                            sendRemove();
                        }
                    });

                    observable.sendState();
                }

                @Override
                public void getState(Consumer<Object> valueHandler) {
                    if(obj instanceof JSObject)
                        valueHandler.accept(((JSObject)obj).getMember(member));
                    else
                        valueHandler.accept(obj);
                }

                /*{
                    if(obj instanceof ObservableJSObject) {
                        ((ObservableJSObject) obj).addObserver(change -> {
                            switch ((String) change.get("type")) {
                                case "put":
                                case "update": {
                                    String name = (String) change.get("name");
                                    if (name.equals(member)) {
                                        Object value = change.get("value");
                                        sendNext(value);
                                    }
                                    break;
                                }
                                case "remove": {
                                    String name = (String) change.get("name");
                                    if (name.equals(member)) {
                                        Object value = Undefined.getUndefined();
                                        sendNext(value);
                                    }
                                    break;
                                }
                            }
                        });
                    }
                }*/

                @Override
                public String toString() {
                    return observable + "." + member;
                }
            };
        }

        public Observable memberSourceReplace(Observable observable, String member) {
            return new AbstractObservable() {
                JSObject obj;

                {
                    observable.addObserver(new Observer() {
                        @Override
                        public void next(Object value) {
                            obj = (JSObject) value;
                            sendState();
                        }

                        @Override
                        public void remove() {
                            sendRemove();
                        }
                    });

                    observable.sendState();
                }

                @Override
                public void getState(Consumer<Object> valueHandler) {
                    valueHandler.accept(obj.getMember(member));
                }

                {
                    ((ObservableJSObject) obj).addObserver(change -> {
                        switch ((String)change.get("type")) {
                            case "put": {
                                String name = (String) change.get("name");
                                if(name.equals(member)) {
                                    Object value = change.get("value");
                                    sendNext(value);
                                }
                                break;
                            } case "remove": {
                                String name = (String) change.get("name");
                                if(name.equals(member)) {
                                    Object value = Undefined.getUndefined();
                                    sendNext(value);
                                }
                                break;
                            }
                        }
                    });
                }

                @Override
                public String toString() {
                    return observable + "." + member + " replace";
                }
            };
        }

        public Observer memberTarget(JSObject obj, String member) {
            return new AbstractObservableObserver() {
                {
                    ((ObservableJSObject)obj).put(member, this);
                }

                @Override
                public void getState(Consumer<Object> valueHandler) {
                    valueHandler.accept(obj.getMember(member));
                }

                @Override
                public void next(Object value) {
                    //obj.setMember(member, value);
                    sendNext(value);
                }

                @Override
                public String toString() {
                    return obj + "." + member;
                }
            };
        }

        public Observer memberTargetUpdate(JSObject obj, String member) {
            return new AbstractObservableObserver() {
                @Override
                public void getState(Consumer<Object> valueHandler) {
                    valueHandler.accept(obj.getMember(member));
                }

                @Override
                public void next(Object value) {
                    ((ObservableJSObject)obj).update(member);
                }

                @Override
                public String toString() {
                    return obj + "." + member + "!";
                }
            };
        }

        public Observable either(Observable[] observables) {
            return new AbstractObservable() {
                private Object currentValue;

                {
                    Arrays.asList(observables).forEach(x -> {
                        x.addObserver(new Observer() {
                            @Override
                            public void next(Object value) {
                                currentValue = value;
                                sendState();
                            }

                            @Override
                            public void remove() {
                                sendRemove();
                            }
                        });
                    });
                }

                @Override
                public void getState(Consumer<Object> valueHandler) {
                    valueHandler.accept(currentValue);
                }

                @Override
                public String toString() {
                    return "either(" + Arrays.asList(observables) + ")";
                }
            };
        }

        public Observable dependent(Observable dependency, Observable dependent) {
            return new AbstractObservable() {
                {
                    dependency.addAndReturnObserver(new Observer() {
                        @Override
                        public void next(Object value) {
                            sendState();
                        }

                        @Override
                        public void remove() {
                            sendRemove();
                        }
                    });
                }

                @Override
                public void getState(Consumer<Object> valueHandler) {
                    dependent.getState(valueHandler);
                }

                @Override
                public String toString() {
                    return dependent + ": " + dependency;
                }
            };
        }

        public Observable constSource(Object obj) {
            /*return new AbstractObservable() {
                @Override
                public void getState(Consumer<Object> valueHandler) {
                    valueHandler.accept(obj);
                }

                @Override
                public String toString() {
                    return obj.toString();
                }
            };*/

            return statConstSource(obj);
        }

        public static Observable statConstSource(Object obj) {
            return new AbstractObservable() {
                @Override
                public void getState(Consumer<Object> valueHandler) {
                    valueHandler.accept(obj);
                }

                @Override
                public String toString() {
                    return obj.toString();
                }
            };
        }

        /*public Observable changed(Object value, Observable observable) {
            return new AbstractObservable() {
                private Object otherValue;
                private Observer observer;

                {
                    observer = observable.addAndReturnObserver(new Observer() {
                        @Override
                        public void next(Object value) {
                            otherValue = value;
                            sendState();
                        }

                        @Override
                        public void remove() {
                            sendRemove();
                        }
                    });
                    observable.sendState();
                }

                @Override
                public void getState(Consumer<Object> valueHandler) {
                    try {
                        Method m = ScriptObjectMirror.class.getDeclaredMethod("getScriptObject");
                        m.setAccessible(true);
                        valueHandler.accept(m.invoke(value) != m.invoke(otherValue));
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    //valueHandler.accept(value != otherValue);
                    //valueHandler.accept(((ScriptObjectMirror)value). != otherValue);
                }

                @Override
                public String toString() {
                    return value + " != " + observable;
                }
            };
        }*/

        public Observable until(Observable observable, Observable observableCondition) {
            return new AbstractObservable() {
                Observer observer;
                Observer conditionObserver;

                {
                    observer = observable.addAndReturnObserver(new Observer() {
                        @Override
                        public void next(Object value) {
                            sendNext(value);
                        }

                        @Override
                        public void remove() {
                            if(conditionObserver != null) {
                                observableCondition.removeObserver(conditionObserver);
                                sendRemove();
                            }
                        }
                    });

                    conditionObserver = observableCondition.addAndReturnObserver(new Observer() {
                        @Override
                        public void next(Object value) {
                            observableCondition.removeObserver(conditionObserver);
                        }

                        @Override
                        public void remove() {
                            conditionObserver = null;
                            observable.removeObserver(observer);
                            sendRemove();
                        }
                    });
                }

                @Override
                public void getState(Consumer<Object> valueHandler) {
                    observable.getState(valueHandler);
                }

                @Override
                public String toString() {
                    return observable + " until " + observableCondition;
                }
            };
        }

        public Observable reducer(Observable[] observables, Object reduceFunc) {
            return new AbstractObservable() {
                private Object[] arguments = new Object[observables.length];

                {
                    IntStream.range(0, observables.length).forEach(i -> {
                        observables[i].addObserver(new Observer() {
                            @Override
                            public void next(Object value) {
                                arguments[i] = value;

                                sendState();
                            }

                            @Override
                            public void remove() {
                                sendRemove();
                            }
                        });
                        observables[i].sendState();
                    });
                }

                /*@Override
                public void sendState() {
                    if(Arrays.asList(arguments).stream().allMatch(x -> x != null)) {
                        Object value = ((ScriptObjectMirror)reduceFunc).call(null, arguments);
                        sendNext(value);
                    }
                }*/

                @Override
                public void getState(Consumer<Object> valueHandler) {
                    if(Arrays.asList(arguments).stream().allMatch(x -> x != null)) {
                        Object value = ((ScriptObjectMirror)reduceFunc).call(null, arguments);
                        valueHandler.accept(value);
                    }
                }

                @Override
                public String toString() {
                    return reduceFunc.toString() +
                        "(" + Arrays.asList(observables).stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + ")";
                }
            };
        }

        public Binding bind(Object source, Object target) {
            Binding binding = new Binding() {
                Observer sourceObserver;
                Observer targetObserver;

                {
                    // Add observers to source and target that observes removal
                    // and ensures this binding only exists until either of
                    // these observers receives a remove message.

                    sourceObserver = ((Observable)source).addAndReturnObserver(new Observer() {
                        @Override
                        public void next(Object value) {

                        }

                        @Override
                        public void remove() {
                            sourceObserver = null;
                            if(targetObserver != null)
                                ((Observable)target).removeObserver(targetObserver);
                        }
                    });
                    targetObserver = ((Observable)target).addAndReturnObserver(new Observer() {
                        @Override
                        public void next(Object value) {

                        }

                        @Override
                        public void remove() {
                            targetObserver = null;
                            if(sourceObserver != null)
                                ((Observable)source).removeObserver(sourceObserver);
                        }
                    });
                }

                @Override
                public Object getSource() {
                    return source;
                }

                @Override
                public Object getTarget() {
                    return target;
                }

                @Override
                public void remove() {
                    ((Removable)source).remove();
                    ((Removable)target).remove();

                    bindings.remove(this);
                }

                @Override
                public String toString() {
                    return getTarget() + " = " + getSource();
                }
            };

            // Remove any existing binding for the target; at most 1 binding exist for a target
            // TODO: Implement equals for source- and target types
            bindings.stream()
                .filter(x -> x.getTarget().equals(target)).findFirst()
                .ifPresent(b -> b.remove());

            // The binding should be removed if source or target is removed

            bindings.add(binding);

            ((Observable)source).addObserver(target);
            ((Observable)source).sendState();
            //source.call("addObserver", target);

            return binding;
        }
    }

    private static void makeInteractive(NashornScriptEngine engine, JPanel desktop, JComponent component, ObservableJSObject base, Map<String, ObservableJSObject> indexedObjects, Facade core) {
        component.addMouseListener(new MouseAdapter() {
            private JComponent pressComponent;
            private ObservableJSObject thisObject;
            private ObservableJSObject thatObject;

            @Override
            public void mousePressed(MouseEvent e) {
                pressComponent = (JComponent) component.findComponentAt(e.getPoint());
                thisObject = pressComponent != component
                    ? ((JSComponent)pressComponent).object : base;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JComponent releaseComponent = (JComponent) component.findComponentAt(e.getPoint());
                thatObject = releaseComponent != component
                    ? ((JSComponent)releaseComponent).object : base;
                /*ObservableJSObject cellObject = releaseComponent != component
                    ? ((JSComponent)releaseComponent).object : base;*/

                if(e.getButton() == MouseEvent.BUTTON1) {
                    //JTextField singleInteractive = new JTextField();
                    JTextArea singleInteractive = new JTextArea("");

                    singleInteractive.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            singleInteractive.setSize(singleInteractive.getPreferredSize());
                            singleInteractive.repaint();
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            singleInteractive.setSize(singleInteractive.getPreferredSize());
                            singleInteractive.repaint();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            singleInteractive.setSize(singleInteractive.getPreferredSize());
                            singleInteractive.repaint();
                        }
                    });

                    singleInteractive.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
                    singleInteractive.setLocation(e.getPoint());
                    singleInteractive.setSize(10, 20);
                    singleInteractive.setForeground(Color.WHITE);
                    singleInteractive.setBackground(Color.DARK_GRAY);
                    singleInteractive.setCaretColor(singleInteractive.getForeground());
                    singleInteractive.registerKeyboardAction(e1 -> {
                        try {
                            singleInteractive.getDocument().insertString(singleInteractive.getCaretPosition(), "\n", null);
                        } catch (BadLocationException e2) {
                            e2.printStackTrace();
                        }
                    }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.MODIFIER_ALT, false), JComponent.WHEN_FOCUSED);
                    singleInteractive.registerKeyboardAction(e1 -> {
                        engine.put("that", thatObject);
                        Object result = eval(engine, singleInteractive, thisObject);

                        if(releaseComponent == desktop)
                            put(result, base, engine, desktop, indexedObjects, e.getPoint(), core);

                        desktop.remove(singleInteractive);
                        desktop.repaint();
                        desktop.revalidate();
                    }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);
                    singleInteractive.registerKeyboardAction(e1 -> {
                        desktop.remove(singleInteractive);
                        desktop.repaint();
                        desktop.revalidate();
                    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_FOCUSED);
                    singleInteractive.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            desktop.remove(singleInteractive);
                            desktop.repaint();
                            desktop.revalidate();
                        }
                    });

                    desktop.add(singleInteractive);
                    desktop.setComponentZOrder(singleInteractive, 0);
                    desktop.revalidate();
                    desktop.repaint();
                    singleInteractive.requestFocusInWindow();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                JComponent clickComponent = (JComponent) component.findComponentAt(e.getPoint());
                ObservableJSObject cellObject = clickComponent != component
                    ? ((JSComponent)clickComponent).object : base;

                switch(e.getButton()) {
                    case MouseEvent.BUTTON1:
                        //JTextField singleInteractive = new JTextField();
                        JTextArea singleInteractive = new JTextArea("");

                        singleInteractive.getDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                singleInteractive.setSize(singleInteractive.getPreferredSize());
                                singleInteractive.repaint();
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                singleInteractive.setSize(singleInteractive.getPreferredSize());
                                singleInteractive.repaint();
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                singleInteractive.setSize(singleInteractive.getPreferredSize());
                                singleInteractive.repaint();
                            }
                        });

                        singleInteractive.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
                        singleInteractive.setLocation(e.getPoint());
                        singleInteractive.setSize(10, 20);
                        singleInteractive.setForeground(Color.WHITE);
                        singleInteractive.setBackground(Color.DARK_GRAY);
                        singleInteractive.setCaretColor(singleInteractive.getForeground());
                        singleInteractive.registerKeyboardAction(e1 -> {
                            try {
                                singleInteractive.getDocument().insertString(singleInteractive.getCaretPosition(), "\n", null);
                            } catch (BadLocationException e2) {
                                e2.printStackTrace();
                            }
                        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.MODIFIER_ALT, false), JComponent.WHEN_FOCUSED);
                        singleInteractive.registerKeyboardAction(e1 -> {
                            Object result = eval(engine, singleInteractive, cellObject);

                            put(result, base, engine, desktop, indexedObjects, e.getPoint(), core);

                            desktop.remove(singleInteractive);
                            desktop.repaint();
                            desktop.revalidate();
                        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);
                        singleInteractive.registerKeyboardAction(e1 -> {
                            desktop.remove(singleInteractive);
                            desktop.repaint();
                            desktop.revalidate();
                        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_FOCUSED);
                        singleInteractive.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusLost(FocusEvent e) {
                                desktop.remove(singleInteractive);
                                desktop.repaint();
                                desktop.revalidate();
                            }
                        });

                        desktop.add(singleInteractive);
                        desktop.setComponentZOrder(singleInteractive, 0);
                        desktop.revalidate();
                        desktop.repaint();
                        singleInteractive.requestFocusInWindow();

                        break;
                    case MouseEvent.BUTTON3:
                        JComponent evalPanel = newInteraction(engine, desktop, cellObject);

                        //evalPanel.setLocation((cell.getX() + cell.getWidth() / 2) - evalPanel.getWidth() / 2, cell.getY() + cell.getHeight());
                        evalPanel.setLocation(e.getPoint());
                        desktop.add(evalPanel);
                        desktop.setComponentZOrder(evalPanel, 0);
                        evalPanel.requestFocusInWindow();

                        desktop.revalidate();
                        desktop.repaint();
                        break;
                }
            }
        });
    }

    private static void put(Object result, ObservableJSObject base, NashornScriptEngine engine, JPanel desktop, Map<String, ObservableJSObject> indexedObjects, Point location, Facade core) {
        if(result instanceof JSObject) {
            // Use object to create cell
            JSObject jsResult = (JSObject) result;
            if(jsResult.keySet().contains("paint")) {
                ObservableJSObject newResult = new ObservableJSObject(base);
                jsResult.keySet().forEach(name ->
                    newResult.put(name, jsResult.getMember(name)));
                result = newResult;
            }
        }

        if(!(result instanceof ObservableJSObject)) {
            if(!(result instanceof Observable)) {
                result = core.constSource(result);
            }

            ObservableJSObject newResult = new ObservableJSObject(base);
            // Should bind until value is changed not sourced from result
            // Perhaps, when new bindings are created then the existing bindings should be examined
            // and overridden/removed if necessary
            // Just one member target pr member?
            // Should OJSO's be cognizant about member targets?
            // Should members be lockable? Only one writer pr member?
            // Should each member value be an observable?
            // Then, when member value changes, update changes are sent out?
            //newResult.put("value", result);

            /*
            // Should replace the below, because then the dependencies are automatically added
            String init =
                "#{ this.paint = function(g) {\n" +
                "    g.drawString(this.value.toString(), 0, this.height);\n" +
                "} }#\n" +
                "this.width = 100;\n" +
                "this.height = 20;\n" +
                "";
            */

            //String message = "Hello, StackOverflow!";

            /*BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) image.getGraphics();
            Rectangle2D stringBounds = g2.getFontMetrics().getStringBounds(message, g2);*/

            /*Font defaultFont = new Font("Helvetica", Font.PLAIN, 12);
            double width = defaultFont.getStringBounds(message, new FontRenderContext(defaultFont.getTransform(), false, false)).getWidth();*/

            // When value changes, then width and height should be updated; how should this be expressed?
            /*
            this.text = #{ this.value.toString() }#
            this.stringBounds = #{ function() {
                var BufferedImage = Java.type("java.awt.BufferedImage");
                var image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
                var g = image.getGraphics();
                return g.getFontMetrics().getStringBounds(this.text, g);
            } }#;
            this.width = stringBounds().getWidth();
            this.height = stringBounds().getHeight();
            this.paint = #{ function(g) {
                g.drawString(this.text, 0, this.height);
            } }#;
            */

            // It seems like multiple bind expressions #{...}# aren't properly replaced within eval.
            // It seems to only work with single bind expressions.
            String init =
                "this.text = #this.value.toString();\n" +
                "this.stringBounds = #{ function() {\n" +
                "    var BufferedImage = Java.type(\"java.awt.BufferedImage\");\n" +
                "    var image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);\n" +
                "    var g = image.getGraphics();\n" +
                "    return g.getFontMetrics().getStringBounds(this.text, g);\n" +
                "} };\n" +
                "this.width = stringBounds().getWidth();\n" +
                "this.height = stringBounds().getHeight();\n" +
                "this.paint = #{ function(g) {\n" +
                "    g.drawString(this.text, 0, this.height);\n" +
                "} };" +
                "";
            /*String init =
                "this.width = 100;\n" +
                "this.height = 20;\n" +
                "this.paint = #{ function(g) {\n" +
                "    g.drawString(this.value.toString(), 0, this.height);\n" +
                "} }#;" +
                "";*/
            eval(engine, init, newResult);

            core.bind(result, core.memberTarget(newResult, "value"));

            //newResult.put("width", stringBounds.getWidth());
            //newResult.put("height", stringBounds.getHeight());
            /*core.bind(
                core.until(
                    core.dependent(
                        core.memberSource(core.constSource(newResult), "value"),
                        core.memberSource(core.constSource(newResult), "paint")
                    ),
                    core.memberSourceReplace(core.constSource(newResult), "paint")
                ),
                core.memberTargetUpdate(newResult, "paint")
            );*/
            /*String paintFunctionScript =
                "function(g) {\n" +
                "    g.drawString(this.value.toString(), 0, this.height);\n" +
                //"    g.drawString(this.value.toString(), 0.0, " + -stringBounds.getY() + ");\n" +
                "}";*/
            /*String paintFunctionScript =
                "#{ function(g) {\n" +
                "    g.drawString(this.value.toString(), 0, this.height);\n" +
                "} }#";
            Object paintFunction = eval(engine, paintFunctionScript, newResult);
            newResult.put("paint", paintFunction);*/
            /*core.bind(
                core.dependent(
                    core.memberSource(core.constSource(newResult), "value"),
                    core.constSource(paintFunction)
                ),
                core.memberTarget(newResult, "paint")
            );*/
            result = newResult;
        }

        if(result instanceof ObservableJSObject) {
            // If already shown, navigate to that component

            JSComponent cellComponent = createCell(engine, desktop, (ObservableJSObject) result, indexedObjects, base);

            ((ObservableJSObject) result).setMember("x", location.x);
            ((ObservableJSObject) result).setMember("y", location.y);

            cellComponent.object.sendState();

            desktop.add(cellComponent);

            //return cellComponent.object;

        } else {
            // Wrap into const something
            // Or?...
        }
    }

    private static midmod.parse.Parser<Character, Character> bindExpressionReplacer = new midmod.parse.Parser<Character, Character>() {
        private midmod.parse.Parser<Character, Character> replaceBindExpressions =
            Parsers.replace(
                Parsers.<Character>seq("#{").skip().then(Parsers.ref(() ->
                    this.replaceBindExpressions).then(Parsers.<Character>seq("}").skip())),
                (captured, output) -> {
                    String partSrc = captured.peekChars().toString();

                    String part = Parser.replace(partSrc);

                    output.putChars(part);

                    /*captured.toString();
                    output.putChar('<');
                    output.putChar('#');
                    while(!captured.atEnd()) {
                        captured.copyTo(output);
                        captured.consume();
                    }
                    output.putChar('>');*/
                }
            ).or(
                Parsers.<Character>seq("}").not()
            ).repeat();

        @Override
        public boolean parse(Input<Character> input, Output<Character> output) {
            return replaceBindExpressions.parse(input, output);
        }
    };

    public static void main(String[] args) {

        /*midmod.parse.Parser<Character, Character> p = new midmod.parse.Parser<Character, Character>() {
           private midmod.parse.Parser<Character, Character> replaceBindExpressions =
                Parsers.replace(
                    Parsers.<Character>seq("#{").skip().then(Parsers.ref(() ->
                        this.replaceBindExpressions).then(Parsers.<Character>seq("}").skip())),
                    (captured, output) -> {
                        captured.toString();
                        output.putChar('<');
                        output.putChar('#');
                        while(!captured.atEnd()) {
                            captured.copyTo(output);
                            captured.consume();
                        }
                        output.putChar('>');
                    }
                ).or(
                    Parsers.<Character>seq("}").not()
                ).repeat();

            @Override
            public boolean parse(Input<Character> input, Output<Character> output) {
                return replaceBindExpressions.parse(input, output);
            }
        };

        p.parse(new CharSequenceInput("asf#{sdf}fg"), new CharSequenceOutput(new StringBuilder()));
        StringBuilder sb = new StringBuilder();
        p.parse(new CharSequenceInput("a#{f #{j} } b  #{fg} ;"), new CharSequenceOutput(sb));

        sb.toString();
        if(1 != 2)
            return;
        */

        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");

        JFrame frame = new JFrame("jsflow");
        JPanel desktop = (JPanel) frame.getContentPane();
        desktop.setLayout(null);

        ObservableJSObject base = new ObservableJSObject(null);

        Facade core = new Facade(engine, desktop, base);
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("core", core);

        JPopupMenu contextMenu = new JPopupMenu();


        makeInteractive(engine, desktop, desktop, base, core.indexedObjects, core);

        /*desktop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switch(e.getClickCount()) {
                    case 1:
                        break;
                    case 2:
                        JComponent clickComponent = (JComponent) desktop.findComponentAt(e.getPoint());
                        ObservableJSObject cellObject = clickComponent != desktop
                            ? ((JSComponent)clickComponent).object : base;
                        JComponent evalPanel = newInteraction(engine, desktop, cellObject);

                        //evalPanel.setLocation((cell.getX() + cell.getWidth() / 2) - evalPanel.getWidth() / 2, cell.getY() + cell.getHeight());
                        evalPanel.setLocation(e.getPoint());
                        desktop.add(evalPanel);
                        evalPanel.requestFocusInWindow();

                        desktop.revalidate();
                        desktop.repaint();
                        break;
                }
            }
        });*/

        try {
            //engine.eval("base = {}");
            //engine.eval("base.paint = function(g) { g.setColor(this.background); g.fillRect(0, 0, this.width, this.height); }");
            //engine.setBindings(base, ScriptContext.GLOBAL_SCOPE);
            //engine.setBindings(base, ScriptContext.ENGINE_SCOPE);
            //engine.eval("foo", base);
            //engine.compile("foo").eval(base);
            ScriptObjectMirror function = (ScriptObjectMirror) engine.eval("function() {\n" +
                "   this.width = 20;\n" +
                "   this.height = 20;\n" +
                "   this.paint = function(g) { g.setColor(this.background); g.fillRect(0, 0, this.width, this.height); }\n" +
                "   this.clone = function() { return core.clone(this); }\n" +
                "}");
            function.call(base);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        //base.eval("this.paint = function(g) { g.setColor(this.background); g.fillRect(0, 0, this.width, this.height); }");

        //SimpleBindings baseBindings = new SimpleBindings();
        //engine.eval("function paint(g) { g.setColor(background); g.fillRect(0, 0, width, height); }");
        //ScriptObjectMirror base = (ScriptObjectMirror)baseBindings.get("nashorn.global");

        contextMenu.add(new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point clickPoint = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(clickPoint, desktop);

                SimpleBindings bindings = new SimpleBindings();

                //object.put("base", base);

                //ObservableJSObject cellObject = new ObservableJSObject(base);
                /*ScriptObjectMirror cellObject = null;

                try {
                    //engine.eval("function paint(g) { g.setColor(background); g.fillRect(0, 0, width, height); }", object);
                    cellObject = (ScriptObjectMirror)engine.eval("var n = {}; Object.setPrototypeOf(n, base);  n;");
                    //obj.toString();
                } catch (ScriptException e1) {
                    e1.printStackTrace();
                }*/

                //ScriptObjectMirror cellObject = (ScriptObjectMirror)object.get("nashorn.global");
                //cellObject.setProto(base);

                //JSComponent cell = new JSComponent(cellObject);
                //JComponent cell = createCell(engine, desktop, cellObject, core.indexedObjects);
                //JComponent cell = core.clone(base);
                ObservableJSObject cellObject = core.clone(base);

                cellObject.setMember("x", mouseClickPoint.x);
                cellObject.setMember("y", mouseClickPoint.y);
                cellObject.setMember("width", 20);
                cellObject.setMember("height", 20);
                cellObject.setMember("background", Color.BLUE);
                //cell.setLocation(mouseClickPoint);
                //cell.setSize(20, 20);
                //cell.setBackground(Color.BLUE);
                //desktop.add(cell);
                /*desktop.repaint();
                desktop.revalidate();*/
            }
        });
        //desktop.setComponentPopupMenu(contextMenu);

        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static class ObservableJSObject extends AbstractJSObject implements Bindings {
        private ObservableJSObject proto;
        private Consumer<Map<String, Object>> protoObserver;
        //private Hashtable<String, Object> members = new Hashtable<>();
        private Hashtable<String, Member> members = new Hashtable<>();
        private ArrayList<Consumer<Map<String, Object>>> observers = new ArrayList<>();

        public void addObserver(Consumer<Map<String, Object>> observer) {
            observers.add(observer);
        }

        public void removeObserver(Consumer<Map<String, Object>> observer) {
            observers.remove(observer);
        }

        private ObservableJSObject(ObservableJSObject proto) {
            this.proto = proto;

            if(proto != null) {
                protoObserver = change -> {
                    switch ((String) change.get("type")) {
                        case "put":
                        case "update":
                        case "remove": {
                            String name = (String) change.get("name");
                            if (!containsKey(name))
                                sendChange(change);
                        }
                    }
                };
                proto.addObserver(protoObserver);
            }
        }

        public void cleanup() {
            proto.removeObserver(protoObserver);
            proto = null;
        }

        @Override
        public Object getMember(String name) {
            Member value = members.get(name);
            if(value != null)
                return value.value;
            if(proto != null)
                return proto.getMember(name);
            return Undefined.getUndefined();
        }

        @Override
        public void setMember(String name, Object value) {
            put(name, value);
        }

        public void update(String name) {
            sendChange(newMap(change -> {
                change.put("type", "update");
                change.put("name", name);
                change.put("value", get(name));
            }));
        }

        private class Member implements Observer {
            private String name;
            private Observable observable;
            private Object value;

            private Member(String name, Observable observable) {
                this.name = name;
                this.observable = observable;

                observable.addObserver(this);
                observable.sendState();
            }

            @Override
            public void next(Object value) {
                boolean firstValue = this.value == null;

                this.value = value;

                String type = firstValue ? "put" : "update";
                sendChange(newMap(change -> {
                    change.put("type", type);
                    change.put("name", name);
                    change.put("value", value);
                }));
            }

            @Override
            public void remove() {
                if(observable != null) {
                    Observable tmpObs = observable;
                    observable = null;
                    tmpObs.removeObserver(this);
                }
            }
        }

        @Override
        public Object put(String name, Object value) {
            Member currentMember = members.get(name);
            if(currentMember != null)
                currentMember.remove();

            Observable observableValue;
            if(value instanceof Observable)
                observableValue = (Observable)value;
            else
                observableValue = Facade.statConstSource(value);

            //Object prevValue = members.put(name, value);
            Object prevValue = members.put(name, new Member(name, observableValue));

            /*sendChange(newMap(change -> {
                change.put("type", "put");
                change.put("name", name);
                change.put("value", value);
            }));*/

            return prevValue;
        }

        private Map<String, Object> newMap(Consumer<Map<String, Object>> mapPopulator) {
            Map<String, Object> map = new Hashtable<>();
            mapPopulator.accept(map);
            return map;
        }

        private void sendChange(Map<String, Object> change) {
            // Perhaps, a set of the objects that have handled the change, should be part of a change
            observers.forEach(o -> o.accept(change));
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> toMerge) {
            toMerge.entrySet().forEach(e ->
                setMember(e.getKey(), e.getValue()));

            sendChange(newMap(change -> {
                change.put("type", "putAll");
                change.put("toMerge", toMerge);
            }));
        }

        @Override
        public void clear() {
            members.keySet().forEach(name -> remove(name));

            sendChange(newMap(change -> {
                change.put("type", "clear");
            }));
        }

        @Override
        public Set<String> keySet() {
            Set<String> keySet = new HashSet<>();
            if(proto != null)
                keySet.addAll(proto.keySet());
            keySet.addAll(members.keySet());
            return keySet;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return keySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x, getMember(x))).collect(Collectors.toSet());
        }

        @Override
        public int size() {
            return members.size();
        }

        @Override
        public boolean isEmpty() {
            return members.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return members.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return members.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            return getMember((String) key);
        }

        @Override
        public void removeMember(String name) {
            remove(name);
        }

        @Override
        public Object remove(Object key) {
            Object value = members.remove(key);

            Object resolvedValue = get(key);

            if(resolvedValue == null) {
                sendChange(newMap(change -> {
                    change.put("type", "remove");
                    change.put("name", key);
                    change.put("value", value);
                }));
            } else {
                sendChange(newMap(change -> {
                    change.put("type", "put");
                    change.put("name", key);
                    change.put("value", value);
                }));
            }

            return value;
        }

        public void sendState() {
            entrySet().forEach(e -> sendChange(newMap(change -> {
                change.put("type", "update");
                change.put("name", e.getKey());
                change.put("value", e.getValue());
            })));
        }

        @Override
        public Object eval(String s) {
            return super.eval(s);
        }
    }

    private static JSComponent createCell(NashornScriptEngine engine, JPanel desktop, ObservableJSObject cellObject, Map<String, ObservableJSObject> indexedObjects, ObservableJSObject base) {
        JSComponent cell = new JSComponent(cellObject);
        //makeInteractive(engine, desktop, cell, base, indexedObjects);

        /*cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                e.toString();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                e.toString();
            }
        });*/

        cellObject.addObserver(new Consumer<Map<String, Object>>() {
            String currentName;

            @Override
            public void accept(Map<String, Object> change) {
                switch ((String)change.get("type")) {
                    case "put": {
                        String name = (String) change.get("name");
                        if(name.equals("name")) {
                            if(currentName != null)
                                indexedObjects.remove(currentName);

                            Object value = change.get("value");
                            indexedObjects.put((String)value, cellObject);

                            currentName = name;
                        }
                        break;
                    } case "remove": {
                        String name = (String) change.get("name");
                        if(name.equals("name")) {
                            indexedObjects.remove(name);
                            currentName = null;
                        }
                        break;
                    }
                }
            }
        });

        cellObject.addObserver(change -> {
            System.out.println("Received change: " + change);

            if(change.get("type").equals("put") || change.get("type").equals("update")) {
                Object value = change.get("value");
                boolean requiresUpdate = false;

                switch((String)change.get("name")) {
                    case "x":
                        cell.setLocation(((Number) value).intValue(), cell.getY());
                        requiresUpdate = true;
                        break;
                    case "y":
                        cell.setLocation(cell.getX(), ((Number)value).intValue());
                        requiresUpdate = true;
                        break;
                    case "width":
                        cell.setSize(((Number)value).intValue(), cell.getHeight());
                        requiresUpdate = true;
                        break;
                    case "height":
                        cell.setSize(cell.getWidth(), ((Number)value).intValue());
                        requiresUpdate = true;
                        break;
                    case "background":
                        cell.setBackground((Color)value);
                        requiresUpdate = true;
                        break;
                    case "paint":
                        requiresUpdate = true;
                        break;
                }

                if(requiresUpdate) {
                    desktop.revalidate();
                    desktop.repaint();
                }
            }
        });

        JPopupMenu cellContextMenu = new JPopupMenu();

        cellContextMenu.add(new AbstractAction("clone") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleBindings bindings = new SimpleBindings();

                bindings.put("base", cellObject);
                ObservableJSObject newCellObject = null;
                newCellObject = new ObservableJSObject(cellObject);

                JComponent clone = createCell(engine, desktop, newCellObject, indexedObjects, base);



                newCellObject.setMember("x", cell.getX() + cell.getWidth() + 5);
                newCellObject.setMember("y", cell.getY());
                newCellObject.sendState();

                desktop.add(clone);
                desktop.revalidate();
                desktop.repaint();
            }
        });

        cellContextMenu.add(new AbstractAction("interact") {
            private Object eval(JTextPane script) {
                String text = script.getSelectedText();
                if (text == null)
                    text = script.getText();

                ScriptObjectMirror function = null;
                try {
                    function = (ScriptObjectMirror) engine.eval("function(text) { return eval(text); }");
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
                return function.call(cellObject, text);

                //return cellObject.eval(text);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel evalPanel = new JPanel();
                JToolBar toolBar = new JToolBar();
                JTextPane script = new JTextPane();
                evalPanel.setLayout(new BorderLayout());
                toolBar.add(new AbstractAction("close") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        desktop.remove(evalPanel);
                        desktop.revalidate();
                        desktop.repaint();
                    }
                });
                toolBar.add(new AbstractAction("run") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        eval(script);
                    }
                });
                toolBar.add(new AbstractAction("print") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Object obj = eval(script);
                        int insertIndex;
                        if (script.getSelectionStart() == script.getSelectionEnd())
                            insertIndex = script.getDocument().getLength();
                        else
                            insertIndex = script.getSelectionEnd();
                        try {
                            String output = obj.toString();
                            script.getStyledDocument().insertString(insertIndex, output, null);
                            script.select(insertIndex, insertIndex + output.length());
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                        Object uw = jdk.nashorn.api.scripting.ScriptUtils.unwrap(cellObject);
                    }
                });
                evalPanel.add(toolBar, BorderLayout.NORTH);
                evalPanel.add(new JScrollPane(script), BorderLayout.CENTER);
                evalPanel.setSize(320, 200);

                evalPanel.setLocation((cell.getX() + cell.getWidth() / 2) - evalPanel.getWidth() / 2, cell.getY() + cell.getHeight());
                desktop.add(evalPanel);

                desktop.revalidate();
                desktop.repaint();
            }
        });

        //cell.setComponentPopupMenu(cellContextMenu);
        return cell;
    }

    private static Object eval(NashornScriptEngine engine, JTextComponent script, ObservableJSObject cellObject) {
        String text = script.getSelectedText();
        if (text == null)
            text = script.getText();

        return eval(engine, text, cellObject);

        /*Pattern pattern = Pattern.compile("#\\{.*\\}");
        Matcher matcher = pattern.matcher(text);

        StringBuffer newText = new StringBuffer();
        while(matcher.find()) {
            String part = matcher.group();
            //part = "//<found part>";
            part = Parser.replace(part);
            matcher.appendReplacement(newText, part);

            //newText.append(part);
        }
        matcher.appendTail(newText);
        text = newText.toString();

        System.out.println("Expanded text:\n" + text + "...");

        ScriptObjectMirror function = null;
        try {
            function = (ScriptObjectMirror) engine.eval("function(text) { return eval(text); }");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return function.call(cellObject, text);*/

        //return cellObject.eval(text);
    }

    private static Object eval(NashornScriptEngine engine, String text, ObservableJSObject cellObject) {
        String newText = Parser.replace(text);
        //StringBuilder newText = new StringBuilder();
        //bindExpressionReplacer.parse(new CharSequenceInput(text), new CharSequenceOutput(newText));

        /*Pattern pattern = Pattern.compile("#\\{.*\\}#", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        StringBuffer newText = new StringBuffer();
        while(matcher.find()) {
            String part = matcher.group();
            //part = "//<found part>";
            part = Parser.replace(part);
            matcher.appendReplacement(newText, part);

            //newText.append(part);
        }
        matcher.appendTail(newText);*/
        text = newText.toString();

        System.out.println("Expanded text:\n" + text + "...");

        ScriptObjectMirror function = null;
        try {
            function = (ScriptObjectMirror) engine.eval("function(text) { return eval(text); }");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return function.call(cellObject, text);

        //return cellObject.eval(text);
    }

    private static JComponent newInteraction(NashornScriptEngine engine, JPanel desktop, ObservableJSObject cellObject) {
        JPanel evalPanel = new JPanel();

        JToolBar toolBar = new JToolBar();
        JTextPane script = new JTextPane();
        /*
        Drag and drop text from script to desktop means "eval and put into cell on desktop"
        */
        script.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        evalPanel.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                script.requestFocusInWindow();
            }
        });
        evalPanel.setLayout(new BorderLayout());
        toolBar.add(new AbstractAction("close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                desktop.remove(evalPanel);
                desktop.revalidate();
                desktop.repaint();
            }
        });
        toolBar.add(new AbstractAction("run") {
            @Override
            public void actionPerformed(ActionEvent e) {
                eval(engine, script, cellObject);
            }
        });
        /*toolBar.add(new AbstractAction("put") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object result = eval(engine, script, cellObject);

                put(result, base, engine, desktop, indexedObjects, e.getPoint());
            }
        });*/
        toolBar.add(new AbstractAction("print") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object obj = eval(engine, script, cellObject);
                int insertIndex;
                if (script.getSelectionStart() == script.getSelectionEnd())
                    insertIndex = script.getDocument().getLength();
                else
                    insertIndex = script.getSelectionEnd();
                try {
                    String output = obj.toString();
                    script.getStyledDocument().insertString(insertIndex, output, null);
                    script.select(insertIndex, insertIndex + output.length());
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                Object uw = jdk.nashorn.api.scripting.ScriptUtils.unwrap(cellObject);
            }
        });
        evalPanel.add(toolBar, BorderLayout.NORTH);
        evalPanel.add(new JScrollPane(script), BorderLayout.CENTER);
        evalPanel.setSize(320, 200);

        return evalPanel;
    }
}
