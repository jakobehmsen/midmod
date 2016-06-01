package paidia;

import com.sun.glass.events.KeyEvent;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConstructorCell implements Value {
    private String initialSource;
    private Parameter parameter;
    private Function<String, Value> componentParser;

    public ConstructorCell(String initialSource, Function<String, Value> componentParser) {
        this.initialSource = initialSource;
        this.componentParser = componentParser;
    }

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
        JTextArea constructor = new JTextArea(initialSource);

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
        int theHeight = height * initialSource.split("\r\n|\r|\n").length;
        view.setSize(200, theHeight);
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
                int index = constructor.getCaretPosition();
                constructor.getDocument().insertString(index, "\n", null);
            } catch (BadLocationException e2) {
                e2.printStackTrace();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.MODIFIER_ALT), JComponent.WHEN_FOCUSED);

        ((AbstractDocument)constructor.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                String text = string;
                if(text.matches("\r\n|\r|\n")) {
                    int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                    view.setPreferredSize(new Dimension(200, view.getHeight() + additionalHeight * height));
                    view.setSize(view.getPreferredSize());
                }

                super.insertString(fb, offset, string, attr);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                String text = fb.getDocument().getText(offset, length);
                if(text.matches("\r\n|\r|\n")) {
                    int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                    view.setPreferredSize(new Dimension(200, view.getHeight() - additionalHeight * height));
                    view.setSize(view.getPreferredSize());
                }

                super.remove(fb, offset, length);
            }
        });
        /*constructor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    String text = e.getDocument().getText(e.getOffset(), e.getLength());
                    if(text.matches("\r\n|\r|\n")) {
                        int additionalHeight = text.equals("\n") ? 1 : text.split("\r\n|\r|\n").length;
                        view.setPreferredSize(new Dimension(200, view.getHeight() + additionalHeight * height));
                        view.setSize(view.getPreferredSize());
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
        });*/

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

    @Override
    public String toSource() {
        // Should be the current text instead of an immutable?
        return initialSource;
    }
}
