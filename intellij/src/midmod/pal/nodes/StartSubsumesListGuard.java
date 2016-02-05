package midmod.pal.nodes;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;

import java.util.List;

public class StartSubsumesListGuard implements Guard {
    /*private Node node;

    public SubsumesListGuard(Node node) {
        this.node = node;
    }*/

    @Override
    public Node nodeAfter(Node target, /*Should be evaluation context instead*/ Consumable consumable) {
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
            target = Node.match(target, new ListConsumable(list));
            if(target != null) {
                consumable.consume();
                return target;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StartSubsumesListGuard;
    }
}
