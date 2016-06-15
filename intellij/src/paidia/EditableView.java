package paidia;

import com.sun.glass.events.KeyEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class EditableView {
    private TextParser textParseHandler;
    private Editor editor;
    private JTextArea editorComponent;

    public EditableView(TextParser textParseHandler, Editor editor) {
        this.textParseHandler = textParseHandler;
        this.editor = editor;
    }

    public Editor getEditor() {
        return editor;
    }

    public void beginEdit() {
        editorComponent = new JTextArea();
        editorComponent.setText(editor.getText());

        editorComponent.selectAll();

        editor.beginEdit(editorComponent);

        editorComponent.registerKeyboardAction(e1 -> {
            String text = editorComponent.getText();
            JComponent parsedComponent = textParseHandler.parse(editorComponent, text);
            editor.endEdit(parsedComponent);
            listeners.forEach(x -> x.accept((ValueView)parsedComponent));
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        editorComponent.registerKeyboardAction(e1 -> {
            editor.cancelEdit();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        editorComponent.registerKeyboardAction(e1 -> {
            try {
                int index = editorComponent.getCaretPosition();
                editorComponent.getDocument().insertString(index, "\n", null);
            } catch (BadLocationException e2) {
                e2.printStackTrace();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.MODIFIER_ALT), JComponent.WHEN_FOCUSED);

        editorComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                Dimension preferredSize = editorComponent.getUI().getPreferredSize(editorComponent);
                if(editorComponent.getSize().width < preferredSize.width ||
                    editorComponent.getSize().height < preferredSize.height) {
                    preferredSize.setSize(preferredSize.width * 1.5, preferredSize.height);
                    //int caretWidth = 2;
                    //preferredSize.setSize(preferredSize.width + caretWidth, preferredSize.height);
                    editorComponent.setPreferredSize(preferredSize);
                    editorComponent.setSize(preferredSize);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                /*Dimension preferredSize = editorComponent.getUI().getPreferredSize(editorComponent);
                if(editorComponent.getSize().width > preferredSize.width ||
                    editorComponent.getSize().height > preferredSize.height) {
                    int caretWidth = 2;
                    preferredSize.setSize(preferredSize.width + caretWidth, preferredSize.height);
                    int width = Math.max(10, preferredSize.width);
                    int height = Math.max(15, preferredSize.height);
                    editorComponent.setPreferredSize(preferredSize);
                    editorComponent.setSize(width, height);
                }*/
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
    }

    public void cancelEdit() {
        editor.cancelEdit();
    }

    private ArrayList<Consumer<ValueView>> listeners = new ArrayList<>();

    public void addChangeListener(Consumer<ValueView> listener) {
        listeners.add(listener);
    }
}
