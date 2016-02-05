package midmod.pal.nodes;

import midmod.pal.Consumable;

public class EqualsGuard implements Guard {
    private Object obj;

    public EqualsGuard(Object obj) {
        this.obj = obj;
    }

    @Override
    public Node nodeAfter(Node target, Consumable consumable) {
        if(consumable.peek().equals(obj)) {
            consumable.consume();
            return target;
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EqualsGuard && this.obj.equals(((EqualsGuard)obj).obj);
    }
}
