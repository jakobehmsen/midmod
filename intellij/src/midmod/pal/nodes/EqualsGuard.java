package midmod.pal.nodes;

import midmod.pal.Consumable;

import java.util.function.Consumer;

public class EqualsGuard implements Guard {
    private Object obj;

    public EqualsGuard(Object obj) {
        this.obj = obj;
    }

    @Override
    public boolean matches(Consumer<Expression> expressionConsumer, Consumable consumable) {
        if(consumable.peek().equals(obj)) {
            consumable.consume();
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EqualsGuard && this.obj.equals(((EqualsGuard) obj).obj);
    }
}
