package midmod.pal.nodes;

import midmod.pal.Consumable;

public interface Guard {
    boolean accepts(Consumable consumable);
}
