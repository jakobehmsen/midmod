package midmod.pal.nodes;

import midmod.pal.Consumable;

public interface Guard {
    Node nodeAfter(Node target, Consumable consumable);
}
