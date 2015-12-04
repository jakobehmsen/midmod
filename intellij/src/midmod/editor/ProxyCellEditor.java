package midmod.editor;

import midmod.ListCell;
import midmod.MapCell;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class ProxyCellEditor extends JTextArea {
    public ProxyCellEditor() {
        StyledDocument document = new DefaultStyledDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                JComponent replacement;

                if(str.equals("{")) {
                    MapCell mapCell = new MapCell();
                    replacement = new MapCellEditor(mapCell);
                } else if(str.equals("[")) {
                    ListCell listCell = new ListCell();
                    replacement = new ListCellEditor(listCell);
                } else
                    replacement = null;

                if(replacement != null) {
                    int zOrder = getParent().getComponentZOrder(ProxyCellEditor.this);
                    Container parent = ProxyCellEditor.this.getParent();
                    ProxyCellEditor.this.getParent().remove(ProxyCellEditor.this);
                    parent.add(replacement);
                    parent.setComponentZOrder(replacement, zOrder);
                    parent.revalidate();
                    parent.repaint();
                } else {
                    super.insertString(offs, str, a);
                }
            }
        };

        setDocument(document);
    }
}
