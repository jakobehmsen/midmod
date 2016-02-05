package midmod.pal.nodes;

import midmod.pal.Consumable;

public class EndSubsumesListGuard implements Guard {
    @Override
    public Node nodeAfter(Node target, Consumable consumable) {
        // If consumable at end, then the list is consumed and everything is okay
        // and the frame should be popped
        if(consumable.atEnd())
            // popFrame();
            return target;

        // Otherwise, bu-huuuu... :'(
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EndSubsumesListGuard;
    }
}
