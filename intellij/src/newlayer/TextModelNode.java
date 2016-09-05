package newlayer;

import javax.swing.*;
import java.awt.*;

public class TextModelNode implements ModelNode {
    private String text;

    public TextModelNode(String text) {
        this.text = text;
    }

    @Override
    public JComponent buildComponent() {
        JTextPane view = new JTextPane() {
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }

            @Override
            public Insets getInsets() {
                return new Insets(0, 0, 0, 0);
            }
        };
        view.setOpaque(false);
        view.setText(text);
        view.setEditable(false);

        return view;
    }
}
