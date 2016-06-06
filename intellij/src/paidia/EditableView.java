package paidia;

import com.sun.glass.events.KeyEvent;

import javax.swing.*;

public class EditableView {
    private Editor editor;
    private JTextArea editorComponent;

    public EditableView(Editor editor) {
        this.editor = editor;
    }

    public void beginEdit() {
        editorComponent = new JTextArea(editor.getText());

        editorComponent.selectAll();

        editor.beginEdit(editorComponent);

        editorComponent.registerKeyboardAction(e1 -> {
            String text = editorComponent.getText();

            editor.endEdit(text);
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

        editorComponent.registerKeyboardAction(e1 -> {
            editor.cancelEdit();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
    }

    public void cancelEdit() {
        editor.cancelEdit();
    }
}
