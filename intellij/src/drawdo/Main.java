package drawdo;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ScriptObject;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class Main {
    interface ActionEval {
        void eval(Object thiz, int x, int y);

        String getName();
    }

    public static class ActionAdapter {
        ArrayList<Action> actions;
        Object thiz;
        Point point;

        public void addAction(ActionEval actionObject) {
            String name = actionObject.getName();
            Action action = new AbstractAction(name) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actionObject.eval(thiz, point.x, point.y);
                }
            };
            actions.add(action);
        }

        public void addAction(ScriptObjectMirror actionObject) {
            String name = (String)actionObject.get("name");
            Action action = new AbstractAction(name) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actionObject.callMember("do", thiz, point.x, point.y);
                }
            };
            actions.add(action);
        }
    }

    public static void main(String[] args) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        NashornScriptEngine engine = (NashornScriptEngine) engineManager.getEngineByName("nashorn");

        JFrame frame = new JFrame("Drawdo");

        ArrayList<Action> actions = new ArrayList<>();



        ActionAdapter actionAdapter = new ActionAdapter();
        actionAdapter.actions = actions;

        actionAdapter.addAction(new ActionEval() {
            @Override
            public void eval(Object thiz, int x, int y) {
                final JOptionPane optionPane = new JOptionPane(
                    "",
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION);
                JDialog dialog = new JDialog(frame, "sa", true);
                dialog.getContentPane().setLayout(new BorderLayout());
                JTextArea script = new JTextArea();
                dialog.getContentPane().add(new JScrollPane(script), BorderLayout.CENTER);
                dialog.getContentPane().add(optionPane, BorderLayout.SOUTH);
                dialog.setDefaultCloseOperation(
                    JDialog.DISPOSE_ON_CLOSE);
                optionPane.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent e) {
                            String prop = e.getPropertyName();

                            if (dialog.isVisible()
                                && (e.getSource() == optionPane)
                                && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                                //If you were going to check something
                                //before closing the window, you'd do
                                //it here.
                                dialog.setVisible(false);
                            }
                        }
                    });

                dialog.setLocation(MouseInfo.getPointerInfo().getLocation());//  e.getLocationOnScreen());
                dialog.setSize(300, 200);
                //dialog.pack();
                dialog.setVisible(true);

                if (optionPane.getValue().equals(JOptionPane.YES_OPTION)) {
                    String text = script.getText();

                    // Set location and selection
                    // Support put

                    engine.put("x", x);
                    engine.put("y", y);
                    engine.put("actionAdapter", actionAdapter);

                    ScriptObjectMirror function = null;
                    try {
                        function = (ScriptObjectMirror) engine.eval("function(text) { return eval(text); }");
                    } catch (ScriptException ex) {
                        ex.printStackTrace();
                    }
                    function.call(thiz, text);

                    ((JComponent)thiz).repaint();
                    ((JComponent)thiz).revalidate();
                }
            }

            @Override
            public String getName() {
                return "Do";
            }
        });

        JComponent contentPane = (JComponent) frame.getContentPane();

        contentPane.setLayout(null);

        contentPane.addMouseListener(new MouseAdapter() {
            private boolean isPopupTrigger;

            @Override
            public void mousePressed(MouseEvent e) {
                isPopupTrigger = e.isPopupTrigger();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(isPopupTrigger) {
                    JComponent targetComponent = (JComponent) contentPane.findComponentAt(e.getPoint());

                    Point point = SwingUtilities.convertPoint(contentPane, e.getPoint(), targetComponent);

                    actionAdapter.point = point;
                    actionAdapter.thiz = targetComponent;

                    JPopupMenu popupMenu = new JPopupMenu();

                    actions.forEach(a -> popupMenu.add(a));

                    /*popupMenu.add(new AbstractAction("Do") {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            final JOptionPane optionPane = new JOptionPane(
                                "",
                                JOptionPane.PLAIN_MESSAGE,
                                JOptionPane.OK_CANCEL_OPTION);
                            JDialog dialog = new JDialog(frame, "sa", true);
                            dialog.getContentPane().setLayout(new BorderLayout());
                            JTextArea script = new JTextArea();
                            dialog.getContentPane().add(new JScrollPane(script), BorderLayout.CENTER);
                            dialog.getContentPane().add(optionPane, BorderLayout.SOUTH);
                            dialog.setDefaultCloseOperation(
                                JDialog.DISPOSE_ON_CLOSE);
                            optionPane.addPropertyChangeListener(
                                new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent e) {
                                        String prop = e.getPropertyName();

                                        if (dialog.isVisible()
                                            && (e.getSource() == optionPane)
                                            && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                                            //If you were going to check something
                                            //before closing the window, you'd do
                                            //it here.
                                            dialog.setVisible(false);
                                        }
                                    }
                                });

                            dialog.setLocation(e.getLocationOnScreen());
                            dialog.setSize(300, 200);
                            //dialog.pack();
                            dialog.setVisible(true);

                            if (optionPane.getValue().equals(JOptionPane.YES_OPTION)) {
                                String text = script.getText();

                                // Set location and selection
                                // Support put

                                engine.put("x", point.x);
                                engine.put("y", point.y);

                                ScriptObjectMirror function = null;
                                try {
                                    function = (ScriptObjectMirror) engine.eval("function(text) { return eval(text); }");
                                } catch (ScriptException ex) {
                                    ex.printStackTrace();
                                }
                                function.call(targetComponent, text);

                                targetComponent.repaint();
                                targetComponent.revalidate();
                            }
                        }
                    });*/

                    popupMenu.show(targetComponent, point.x, point.y);
                } else {

                }
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
