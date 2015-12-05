package midmod.editor;

import midmod.ListCell;
import midmod.MapCell;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ProxyCellEditor extends /*JTextArea*/ JTextField {
    public ProxyCellEditor(BiConsumer<JComponent, JComponent> replacer) {
        StyledDocument document = new DefaultStyledDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                JComponent replacement = replaceWithComponent(str);

                if(replacement != null) {
                    replacer.accept(ProxyCellEditor.this, replacement);
                } else {
                    super.insertString(offs, str, a);
                    setSize(getPreferredSize());
                }
            }

            @Override
            public void remove(int offs, int len) throws BadLocationException {
                super.remove(offs, len);
                setSize(getPreferredSize());
            }
        };

        setDocument(document);
    }

    protected JComponent replaceWithComponent(String str) {
        return null;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 20);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
