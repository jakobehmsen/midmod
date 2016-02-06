package midmod.pal.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class RepeatNodePopulator implements NodePopulator {
    private NodePopulator populator;

    public RepeatNodePopulator(NodePopulator populator) {
        this.populator = populator;
    }

    @Override
    public void populate(Node source, Node target) {
        // Makes sense here?
    }

    @Override
    public List<Node> populate(Node source) {
        boolean atRepeat = false;
        Node origSource = source;
        ArrayList<Node> intermediates = new ArrayList<>();

        while(true) {
            // TODO: Support multiple targets
            // Multiple targets? What about or's?
            Node target = populator.getTarget(source);
            if(target == null)
                break;
            if(source == target) {
                atRepeat = true;
                break;
            }
            intermediates.add(target);
            source = target;
        }
        // Test whether is at repeat for guard
        if(!atRepeat) {
            // If not, then create repeat
            if (source != origSource) {
                // TODO: Support multiple targets
                source = populator.populate(source).get(0);
            }
            populator.populate(source, source);
        } else {
            // Otherwise, do nothing since the repeat has already been created
        }

        ArrayList<Node> targets = new ArrayList<>();
        targets.addAll(intermediates);

        Node finalTarget = source;

        targets.add(finalTarget);

        return targets;
    }

    @Override
    public Node getTarget(Node source) {
        return null;
    }
}
