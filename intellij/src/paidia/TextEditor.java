package paidia;

import com.sun.glass.events.KeyEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public abstract class TextEditor extends JTextArea {
    public TextEditor() {
        registerKeyboardAction(e1 -> {
            String text = getText();
            endEdit(text);
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        registerKeyboardAction(e1 -> {
            cancelEdit();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                Dimension preferredSize = getUI().getPreferredSize(TextEditor.this);
                if(getSize().width < preferredSize.width ||
                    getSize().height < preferredSize.height) {
                    preferredSize.setSize(preferredSize.width * 1.5, preferredSize.height);
                    //int caretWidth = 2;
                    //preferredSize.setSize(preferredSize.width + caretWidth, preferredSize.height);
                    setPreferredSize(preferredSize);
                    setSize(preferredSize);
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

    protected abstract void endEdit(String text);
    protected abstract void cancelEdit();
}
