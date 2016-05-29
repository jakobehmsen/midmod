package paidia;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.util.Arrays;
import java.util.List;

public class ComponentParser {
    public static Value parse(Workspace workspace, String text) {
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

            /*JLabel view = new JLabel(block.selector().getText());
            view.setSize(((ComponentUI) view.getUI()).getPreferredSize(view));
            return view;*/

            return null;
        } else {
            return parseBlockParts(block.blockPart());
        }
    }

    private static Value parseBlockParts(List<PaidiaParser.BlockPartContext> blockPartContexts) {
        if (blockPartContexts.size() == 0)
            return null;
        else if (blockPartContexts.size() == 1) {
            return parseBlockPart(blockPartContexts.get(0));
        } else {
            // Multiple parts.

            return null;
        }
    }

    private static Value parseBlockPart(PaidiaParser.BlockPartContext blockPartContext) {
        return blockPartContext.accept(new PaidiaBaseVisitor<Value>() {
            @Override
            public Value visitNumber(PaidiaParser.NumberContext ctx) {
                Number number;

                try {
                    number = Long.parseLong(ctx.getText());
                } catch (NumberFormatException e) {
                    number = Double.parseDouble(ctx.getText());
                }

                return new NumberValue(number);
            }
        });
    }
}
