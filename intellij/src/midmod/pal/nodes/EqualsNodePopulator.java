package midmod.pal.nodes;

import java.util.Arrays;
import java.util.List;

public class EqualsNodePopulator implements NodePopulator {
    private Object value;

    public EqualsNodePopulator(Object value) {
        this.value = value;
    }

    @Override
    public void populate(Node source, Node target) {
        source.addEdge(new EqualsGuard(value), target);
    }

    @Override
    public List<Node> populate(Node source) {
        Node target = source.getTargetForEdgeThrough(new EqualsGuard(value));
        /*Node target = new Node();
        source.addEdge(new EqualsGuard(value), target);*/
        return Arrays.asList(target);
    }
}
