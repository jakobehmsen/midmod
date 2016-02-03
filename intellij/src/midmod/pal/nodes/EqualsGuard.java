package midmod.pal.nodes;

import midmod.pal.Consumable;

public class EqualsGuard implements Guard {
    private Object obj;

    public EqualsGuard(Object obj) {
        this.obj = obj;
    }

    @Override
    public boolean accepts(Consumable consumable) {
        if(consumable.peek().equals(obj)) {
            consumable.consume();
            return true;
        }

        return false;
    }
}
