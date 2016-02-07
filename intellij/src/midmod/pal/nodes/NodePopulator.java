package midmod.pal.nodes;

import java.util.List;

public interface NodePopulator {
    default void populate(Node source, Expression expression) {
        populate(source, new PopulationContext() {
            @Override
            public NodePopulator getNextPopulator() {
                return null;
            }

            @Override
            public void addTarget(Node target) {

            }
        }).forEach(x ->
            x.setExpression(expression));
    }
    void populate(Node source, Node target, PopulationContext populationContext);
    List<Node> populate(Node source, PopulationContext populationContext);

    Node getTarget(Node source);
}
