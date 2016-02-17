package jsflow;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.List;

public class Main {
    private static class JSComponent extends JComponent {
        private JSObject object;

        private JSComponent(JSObject object) {
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

        @Override
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
        }
    }

    private static Point mouseClickPoint;

    public static void main(String[] args) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");

        JFrame frame = new JFrame("jsflow");
        JPanel desktop = (JPanel) frame.getContentPane();
        desktop.setLayout(null);

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
            ScriptObjectMirror function = (ScriptObjectMirror) engine.eval("function() {this.paint = function(g) { g.setColor(this.background); g.fillRect(0, 0, this.width, this.height); } }");
            function.call(base);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        //base.eval("this.paint = function(g) { g.setColor(this.background); g.fillRect(0, 0, this.width, this.height); }");

        SimpleBindings baseBindings = new SimpleBindings();
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
                cell.setLocation(mouseClickPoint);
                cell.setSize(20, 20);
                cell.setBackground(Color.BLUE);
                desktop.add(cell);
                /*desktop.repaint();
                desktop.revalidate();*/
            }
        });
        //desktop.setComponentPopupMenu(contextMenu);

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static class ObservableJSObject extends AbstractJSObject implements Bindings {
        private JSObject proto;
        private Hashtable<String, Object> members = new Hashtable<>();

        private ObservableJSObject(JSObject proto) {
            this.proto = proto;
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
            members.put(name, value);
        }

        @Override
        public Object put(String name, Object value) {
            return members.put(name, value);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> toMerge) {
            members.putAll(toMerge);
        }

        @Override
        public void clear() {
            members.clear();;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return members.entrySet();
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
        public Object remove(Object key) {
            return members.remove(key);
        }
    }

    private static JComponent createCell(NashornScriptEngine engine, JPanel desktop, JSObject cellObject) {
        JSComponent cell = new JSComponent(cellObject);

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
                clone.setLocation(new Point(cell.getX() + cell.getWidth() + 5, cell.getY()));
                clone.setSize(20, 20);
                clone.setBackground(Color.RED);
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
