package paidia;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.util.Arrays;
import java.util.List;

public class ComponentParser {
    public static JComponent parse(Workspace workspace, String text) {
        CharStream charStream = new ANTLRInputStream(text);
        PaidiaLexer lexer = new PaidiaLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        PaidiaParser parser = new PaidiaParser(tokenStream);

        PaidiaParser.BlockContext block = parser.block();

        if(block.selector() != null) {
            if(block.selector().ADD_OP() != null || block.selector().MUL_OP() != null) {
                return new CompositeValue(Arrays.asList("lhs", "rhs"), Arrays.asList(new ParameterCell(workspace), new ParameterCell(workspace)), block.selector().getText(), args -> {
                    return null;
                });
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
