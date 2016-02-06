package midmod.pal.nodes;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Consumer;

public class Node {
    private LinkedHashMap<Guard, Node> edges = new LinkedHashMap<>();
    private Expression expression;

    public void addEdge(Guard guard, Node target) {
        edges.put(guard, target);
    }

    public Node match(Consumer<Expression> expressionConsumer, Consumable consumable) {
        Optional<Node> target =
            edges.entrySet().stream().filter(x -> x.getKey().matches(expressionConsumer, consumable)) .map(x -> x.getValue()).findFirst();
        if(target.isPresent()) {
            if(target.get().getExpression() != null)
                expressionConsumer.accept(target.get().getExpression());
            return target.get();
        }
        return null;
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
        Expression[] expressionHolder = new Expression[1];
        node = match(e -> expressionHolder[0] = e, node, consumable);
        if(node != null && expressionHolder[0] != null)
            return expressionHolder[0].evaluate();
        return null;
    }

    public static Node match(Consumer<Expression> expressionConsumer, Node node, Consumable consumable) {
        while(node != null && !consumable.atEnd())
            node = node.match(expressionConsumer, consumable);
        return node;
    }

    public Node getTargetForEdgeThrough(Guard guard) {
        Optional<Node> foundNode = edges.entrySet().stream().filter(x ->
            x.getKey().equals(guard)).map(x -> x.getValue()).findFirst();
        if(foundNode.isPresent())
            return foundNode.get();
        Node node = new Node();
        edges.put(guard, node);
        return node;
    }

    public <T extends Guard> T getGuard(Class<T> guardType) {
        Optional<Guard> foundGuard = edges.entrySet().stream().filter(x ->
            x.getKey().getClass().equals(guardType)).map(x -> x.getKey()).findFirst();
        if(foundGuard.isPresent())
            return (T)foundGuard.get();
        return null;
    }

    public <T extends Guard> Node getTarget(Class<T> guardType) {
        Optional<Node> foundNode = edges.entrySet().stream().filter(x ->
            x.getKey().getClass().equals(guardType)).map(x -> x.getValue()).findFirst();
        if(foundNode.isPresent())
            return foundNode.get();
        return null;
    }
}
