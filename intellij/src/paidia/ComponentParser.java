package paidia;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.util.List;

public class ComponentParser {
    public static JComponent parse(String text) {
        CharStream charStream = new ANTLRInputStream(text);
        PaidiaLexer lexer = new PaidiaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PaidiaParser parser = new PaidiaParser(tokenStream);

        PaidiaParser.BlockContext block = parser.block();

        if(block.selector() != null) {
            if(block.selector().ADD_OP() != null || block.selector().MUL_OP() != null) {
                JTextField lhsView = new JTextField("lhs");
                lhsView.setSize(((ComponentUI) lhsView.getUI()).getPreferredSize(lhsView));
                JLabel selectorView = new JLabel(block.selector().getText());
                selectorView.setSize(((ComponentUI) selectorView.getUI()).getPreferredSize(selectorView));
                JTextField rhsView = new JTextField("rhs");
                rhsView.setSize(((ComponentUI) rhsView.getUI()).getPreferredSize(rhsView));

                JPanel view = new JPanel();

                view.add(lhsView);
                view.add(selectorView);
                view.add(rhsView);

                view.setSize(lhsView.getWidth() + selectorView.getWidth() + rhsView.getWidth() + 25, 30);
                view.setBorder(BorderFactory.createRaisedSoftBevelBorder());

                //view.setSize(((ComponentUI) view.getUI()).getPreferredSize(view));

                return view;
            }

            JLabel view = new JLabel(block.selector().getText());
            view.setSize(((ComponentUI) view.getUI()).getPreferredSize(view));
            return view;
        } else {
            return parseBlockParts(block.blockPart());
        }
    }

    private static JComponent parseBlockParts(List<PaidiaParser.BlockPartContext> blockPartContexts) {
        return null;
    }
}
