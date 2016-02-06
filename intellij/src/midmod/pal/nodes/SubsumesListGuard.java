package midmod.pal.nodes;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;

import java.util.List;
import java.util.function.Consumer;

public class SubsumesListGuard implements Guard {
    private Node node;

    public SubsumesListGuard(Node node) {
        this.node = node;
    }

    @Override
    public boolean matches(/*Should be evaluation context instead*/ Consumer<Expression> expressionConsumer, Consumable consumable) {
        // Should be given a node to proceed from
        // Should return the node to proceed from afterwards, if matches, otherwise null
        if(consumable.peek() instanceof List) {
            List<Object> list = (List<Object>) consumable.peek();

            // TODO: Create evaluation context, where new frames can be pushed
            /*
            // Push frame with new list as consumable
            pushFrame(new ListConsumable(list));
            */

            // Override target
            Node n = Node.match(expressionConsumer, node, new ListConsumable(list));
            if(n != null) {
                consumable.consume();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SubsumesListGuard;
    }

    public Node getNode() {
        return node;
    }
}
