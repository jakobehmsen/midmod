package paidia;

import javax.swing.*;

public interface TextParser {
    void parse(JComponent editorComponent, String text, TextParseHandler handler);
}
