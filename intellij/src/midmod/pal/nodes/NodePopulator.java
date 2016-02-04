package midmod.pal.nodes;

import java.util.List;

public interface NodePopulator {
    default void populate(Node source, Expression expression) {
        populate(source).forEach(x -> x.setExpression(expression));
    }
    void populate(Node source, Node target);
    List<Node> populate(Node source);
}
