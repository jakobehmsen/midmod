package midmod.pal.nodes;

import midmod.pal.Consumable;
import midmod.pal.ListConsumable;

import java.util.List;

public class SubsumesListGuard implements Guard {
    private Node node;

    public SubsumesListGuard(Node node) {
        this.node = node;
    }

    @Override
    public boolean accepts(Consumable consumable) {
        if(consumable.peek() instanceof List) {
            List<Object> list = (List<Object>) consumable.peek();
            Node target = Node.match(node, new ListConsumable(list));
            if(target != null) {
                consumable.consume();
                return true;
            }
        }

        return false;
    }
}
