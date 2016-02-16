package jsflow;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {
    private static class JSComponent extends JComponent {
        private ScriptObjectMirror bindings;

        private JSComponent(ScriptObjectMirror bindings) {
            this.bindings = bindings;
        }

        @Override
        public void paint(Graphics g) {
                bindings.callMember("paint", g);
            //ScriptObjectMirror paintFunction = (ScriptObjectMirror) bindings.get("paint");

            //g.setColor(Color.RED);
            //paintFunction.call(bindings, g);

            //g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            bindings.setMember("background", bg);
        }

        @Override
        public void setLocation(int x, int y) {
            super.setLocation(x, y);
            bindings.setMember("x", x);
            bindings.setMember("y", y);
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);
            bindings.setMember("width", width);
            bindings.setMember("height", height);
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

        try {
            engine.eval("base = {}");
            engine.eval("base.paint = function(g) { g.setColor(this.background); g.fillRect(0, 0, this.width, this.height); }");
        } catch (ScriptException e) {
            e.printStackTrace();
        }

        SimpleBindings baseBindings = new SimpleBindings();
        //engine.eval("function paint(g) { g.setColor(background); g.fillRect(0, 0, width, height); }");
        //ScriptObjectMirror base = (ScriptObjectMirror)baseBindings.get("nashorn.global");

        contextMenu.add(new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point clickPoint = MouseInfo.getPointerInfo().getLocation();
                SwingUtilities.convertPointFromScreen(clickPoint, desktop);

                SimpleBindings bindings = new SimpleBindings();

                //bindings.put("base", base);

                ScriptObjectMirror cellObject = null;

                try {
                    //engine.eval("function paint(g) { g.setColor(background); g.fillRect(0, 0, width, height); }", bindings);
                    cellObject = (ScriptObjectMirror)engine.eval("var n = {}; Object.setPrototypeOf(n, base);  n;");
                    //obj.toString();
                } catch (ScriptException e1) {
                    e1.printStackTrace();
                }

                //ScriptObjectMirror cellObject = (ScriptObjectMirror)bindings.get("nashorn.global");
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

    private static JComponent createCell(NashornScriptEngine engine, JPanel desktop, ScriptObjectMirror cellObject) {
        JSComponent cell = new JSComponent(cellObject);

        JPopupMenu cellContextMenu = new JPopupMenu();

        cellContextMenu.add(new AbstractAction("clone") {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleBindings bindings = new SimpleBindings();

                bindings.put("base", cellObject);
                ScriptObjectMirror newCellObject = null;

                try {
                    //engine.eval("function paint(g) { g.setColor(background); g.fillRect(0, 0, width, height); }", bindings);
                    engine.put("tmp", newCellObject);
                    newCellObject = (ScriptObjectMirror)engine.eval("var n = {}; Object.setPrototypeOf(n, base);  n;");
                    //obj.toString();
                } catch (ScriptException e1) {
                    e1.printStackTrace();
                }

                JComponent clone = createCell(engine, desktop, newCellObject);
                clone.setLocation(new Point(cell.getX() + cell.getWidth() + 5, cell.getY()));
                clone.setSize(20, 20);
                clone.setBackground(Color.RED);
                desktop.add(clone);
                desktop.revalidate();
                desktop.repaint();
            }
        });

        cell.setComponentPopupMenu(cellContextMenu);
        return cell;
    }
}
