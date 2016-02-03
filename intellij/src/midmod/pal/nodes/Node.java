package midmod.pal.nodes;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;

public class Node {
    private LinkedHashMap<Guard, Node> edges = new LinkedHashMap<>();
    private Expression expression;

    public void addEdge(Guard guard, Node target) {
        edges.put(guard, target);
    }

    public Node match(Consumable consumable) {
        Optional<Node> target =
            edges.entrySet().stream().filter(x ->
                x.getKey().accepts(consumable)).map(x -> x.getValue()).findFirst();
        return target.isPresent() ? target.get() : null;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public static Object evaluate(Node node, Object value) {
        return evaluate(node, new ListConsumable(Arrays.asList(value)));
    }

    public static Object evaluate(Node node, Consumable consumable) {
        node = match(node, consumable);
        if(node != null && node.getExpression() != null)
            return node.getExpression().evaluate();
        return null;
    }

    public static Node match(Node node, Consumable consumable) {
        while(node != null && !consumable.atEnd())
            node = node.match(consumable);
        return node;
    }
}
