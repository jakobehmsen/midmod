package midmod.pal.nodes;

import java.util.Arrays;
import java.util.List;

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
        populator.populate(source, source);

        return Arrays.asList(source);
    }
}
