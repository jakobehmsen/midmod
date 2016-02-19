package jsflow;

import jdk.nashorn.api.scripting.*;

import javax.script.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    private static Point mouseClickPoint;
    public static class Facade {
        private NashornScriptEngine engine;
        private JPanel desktop;

        private Facade(NashornScriptEngine engine, JPanel desktop) {
            this.engine = engine;
            this.desktop = desktop;
        }

        public Object clone(Object obj) {
            JSComponent cellComponent = createCell(engine, desktop, new ObservableJSObject((ObservableJSObject) obj));

            cellComponent.object.sendState();

            desktop.add(cellComponent);
            desktop.repaint();
            desktop.revalidate();

            return cellComponent.object;
        }

        @Override
        public String toString() {
            return "blad";
        }
    }

    public static void main(String[] args) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");

        JFrame frame = new JFrame("jsflow");
        JPanel desktop = (JPanel) frame.getContentPane();
        desktop.setLayout(null);

        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("core", new Facade(engine, desktop));

        JPopupMenu contextMenu = new JPopupMenu();

        desktop.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();

                if(contextMenu.isPopupTrigger(e)) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(contextMenu.isPopupTrigger(e)) {
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        ObservableJSObject base = new ObservableJSObject(null);

        try {
            //engine.eval("base = {}");
            //engine.eval("base.paint = function(g) { g.setColor(this.background); g.fillRect(0, 0, this.width, this.height); }");
            //engine.setBindings(base, ScriptContext.GLOBAL_SCOPE);
            //engine.setBindings(base, ScriptContext.ENGINE_SCOPE);
            //engine.eval("foo", base);
            //engine.compile("foo").eval(base);
            ScriptObjectMirror function = (ScriptObjectMirror) engine.eval("function() {\n" +
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

                ObservableJSObject cellObject = new ObservableJSObject(base);
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
                JComponent cell = createCell(engine, desktop, cellObject);
                cellObject.setMember("x", mouseClickPoint.x);
                cellObject.setMember("y", mouseClickPoint.y);
                cellObject.setMember("width", 20);
                cellObject.setMember("height", 20);
                cellObject.setMember("background", Color.BLUE);
                //cell.setLocation(mouseClickPoint);
                //cell.setSize(20, 20);
                //cell.setBackground(Color.BLUE);
                desktop.add(cell);
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
        private Hashtable<String, Object> members = new Hashtable<>();
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
            Object value = members.get(name);
            if(value != null)
                return value;
            if(proto != null)
                return proto.getMember(name);
            return null;
        }

        @Override
        public void setMember(String name, Object value) {
            put(name, value);
        }

        @Override
        public Object put(String name, Object value) {
            Object prevValue = members.put(name, value);

            sendChange(newMap(change -> {
                change.put("type", "put");
                change.put("name", name);
                change.put("value", value);
            }));

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
                change.put("type", "put");
                change.put("name", e.getKey());
                change.put("value", e.getValue());
            })));
        }

        @Override
        public Object eval(String s) {
            return super.eval(s);
        }
    }

    private static JSComponent createCell(NashornScriptEngine engine, JPanel desktop, ObservableJSObject cellObject) {
        JSComponent cell = new JSComponent(cellObject);

        cell.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                e.toString();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                e.toString();
            }
        });

        cellObject.addObserver(change -> {
            System.out.println("Received change: " + change);

            if(change.get("type").equals("put")) {
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

                /*try {
                    //engine.eval("function paint(g) { g.setColor(background); g.fillRect(0, 0, width, height); }", object);
                    engine.put("tmp", newCellObject);

                    newCellObject = new ObservableJSObject(cellObject);

                    //newCellObject = (ScriptObjectMirror)engine.eval("var n = {}; Object.setPrototypeOf(n, base);  n;");
                    //obj.toString();
                } catch (ScriptException e1) {
                    e1.printStackTrace();
                }*/

                JComponent clone = createCell(engine, desktop, newCellObject);



                newCellObject.setMember("x", cell.getX() + cell.getWidth() + 5);
                newCellObject.setMember("y", cell.getY());
                newCellObject.sendState();
                /*cellObject.setMember("width", 20);
                cellObject.setMember("height", 20);
                cellObject.setMember("background", Color.RED);*/

                /*clone.setLocation(new Point(cell.getX() + cell.getWidth() + 5, cell.getY()));
                clone.setSize(20, 20);
                clone.setBackground(Color.RED);*/
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

        cell.setComponentPopupMenu(cellContextMenu);
        return cell;
    }
}
