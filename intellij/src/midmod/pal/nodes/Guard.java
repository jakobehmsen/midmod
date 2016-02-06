package midmod.pal.nodes;

import midmod.pal.Consumable;

import java.util.function.Consumer;

public interface Guard {
    boolean matches(Consumer<Expression> expressionConsumer, Consumable consumable);
}
