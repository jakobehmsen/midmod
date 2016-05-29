package paidia;

import com.sun.glass.events.KeyEvent;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConstructorCell implements Value {
    private Parameter parameter;
    private Function<String, Value> componentParser;

    public ConstructorCell(Function<String, Value> componentParser) {
        //constructor = new JTextArea();

        this.componentParser = componentParser;

        /*add(constructor, BorderLayout.CENTER);

        int height = constructor.getFontMetrics(constructor.getFont()).getHeight();
        setSize(200, height);
        setPreferredSize(getSize());

        constructor.registerKeyboardAction(e1 -> {
            String text = constructor.getText();

            Value view = componentParser.apply(text);

            //parameter.removeValue();
            parameter.replaceValue((Value)view);
            parameter = null;
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        constructor.registerKeyboardAction(e1 -> {
            parameter.removeValue();
            parameter = null;
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        constructor.registerKeyboardAction(e1 -> {
            try {
                constructor.getDocument().insertString(constructor.getDocument().getLength(), "\n", null);
            } catch (BadLocationException e2) {
                e2.printStackTrace();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.MODIFIER_ALT), JComponent.WHEN_FOCUSED);

        constructor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    String text = e.getDocument().getText(e.getOffset(), e.getLength());
                    if(text.matches("\r\n|\r|\n")) {
                        int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                        setSize(200, getHeight() + additionalHeight * height);
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    String text = e.getDocument().getText(e.getOffset(), e.getLength());
                    if(text.matches("\r\n|\r|\n")) {
                        int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                        setSize(200, getHeight() - additionalHeight * height);
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });*/
    }

    /*@Override
    public boolean requestFocusInWindow() {
        return constructor.requestFocusInWindow();
    }*/

    private ArrayList<Runnable> wasBoundListeners = new ArrayList<>();

    @Override
    public void bindTo(Parameter parameter) {
        this.parameter = parameter;
        wasBoundListeners.forEach(x -> x.run());
    }

    @Override
    public void unbind() {
        if(parameter != null) {
            parameter.removeValue();
            parameter = null;
        }
    }

    @Override
    public ViewBinding toComponent() {
        JTextArea constructor = new JTextArea();

        JPanel view = new JPanel(new BorderLayout()) {
            @Override
            public boolean requestFocusInWindow() {
                return constructor.requestFocusInWindow();
            }
        };

        Runnable listener = () -> constructor.requestFocusInWindow();
        wasBoundListeners.add(listener);

        view.add(constructor, BorderLayout.CENTER);

        int height = constructor.getFontMetrics(constructor.getFont()).getHeight();
        view.setSize(200, height);
        view.setPreferredSize(view.getSize());

        constructor.registerKeyboardAction(e1 -> {
            String text = constructor.getText();

            Value value = componentParser.apply(text);

            //parameter.removeValue();
            parameter.replaceValue(value);
            parameter = null;
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        constructor.registerKeyboardAction(e1 -> {
            parameter.removeValue();
            parameter = null;
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        constructor.registerKeyboardAction(e1 -> {
            try {
                constructor.getDocument().insertString(constructor.getDocument().getLength(), "\n", null);
            } catch (BadLocationException e2) {
                e2.printStackTrace();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.MODIFIER_ALT), JComponent.WHEN_FOCUSED);

        constructor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    String text = e.getDocument().getText(e.getOffset(), e.getLength());
                    if(text.matches("\r\n|\r|\n")) {
                        int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                        view.setSize(200, view.getHeight() + additionalHeight * height);
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    String text = e.getDocument().getText(e.getOffset(), e.getLength());
                    if(text.matches("\r\n|\r|\n")) {
                        int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                        view.setSize(200, view.getHeight() - additionalHeight * height);
                    }
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        return new ViewBinding() {
            @Override
            public JComponent getView() {
                return view;
            }

            @Override
            public void release() {
                wasBoundListeners.remove(listener);
            }
        };
    }
}
