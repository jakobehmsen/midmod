package midmod.pal.nodes;

import java.util.ArrayList;
import java.util.List;

public class RepeatNodePopulator implements NodePopulator {
    private NodePopulator populator;

    public RepeatNodePopulator(NodePopulator populator) {
        this.populator = populator;
    }

    @Override
    public void populate(Node source, Node target, PopulationContext populationContext) {
        // Makes sense here?
    }

    @Override
    public List<Node> populate(Node source, PopulationContext populationContext) {
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
                source = populator.populate(source, populationContext).get(0);
            }
            populator.populate(source, source, populationContext);
        } else {
            // Otherwise, do nothing since the repeat has already been created
        }

        ArrayList<Node> targets = new ArrayList<>();
        //targets.addAll(intermediates); // Does not work because expression is set for all of these

        Node finalTarget = source;
        /*
        // Instead of adding intermediates to targets?:
        // For any immediate subsequent populator, connect this populator from each intermediate to distinct targets.
        // This implies that any outer list populator mustn't use this immediate subsequent populator
        intermediates.forEach(i -> populator.populate(i, finalTarget));
        */

        // An outer population context could be provided:

        NodePopulator nextPopulator = populationContext.getNextPopulator();
        if(nextPopulator != null) {
            intermediates.forEach(i -> {
                List<Node> intermediateTargets = nextPopulator.populate(i, populationContext);
                // Each intermediate target should be related to the current expression
                // Currently, they aren't because the targets aren't propagated as targets
                // in the outer list node populator, because the repeat populator isn't the
                // last populator of the list.
                // How to solve this?
                // Create an addTarget method in population context?
                //targets.addAll(intermediateTargets);

                // Add original source
                populationContext.addTarget(origSource);
                // Add all intermediate targets
                intermediateTargets.forEach(x ->
                    populationContext.addTarget(x));
            });
        }

        /*
        Consumer<Consumer<NodePopulator>> nextPopulatorConsumer = null;
        nextPopulatorConsumer.accept(nextPopulator -> {
            intermediates.forEach(i -> nextPopulator.populate(i));
        });
        */

        targets.add(finalTarget);

        return targets;
    }

    @Override
    public Node getTarget(Node source) {
        return null;
    }
}
