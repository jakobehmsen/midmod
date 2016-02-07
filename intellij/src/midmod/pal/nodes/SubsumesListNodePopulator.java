package midmod.pal.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubsumesListNodePopulator implements NodePopulator {
    private List<NodePopulator> populators;

    public SubsumesListNodePopulator(List<NodePopulator> populators) {
        this.populators = populators;
    }

    @Override
    public void populate(Node source, Node target, PopulationContext populationContext) {
        ArrayList<Node> targets = new ArrayList<>();
        List<Node> sources = Arrays.asList(source);

        if(populators.size() > 0)
            populate(sources, 0, target, populationContext);
        else {
            // Not supported
        }
    }

    private void populate(List<Node> sources, int i, Node target, PopulationContext populationContext) {
        sources.forEach(s -> {
            if(i == populators.size() - 1)
                populators.get(i).populate(s, target, populationContext);
            else {
                List<Node> innerTargets = populators.get(i).populate(s, populationContext);
                populate(innerTargets, i + 1, target, populationContext);
            }
        });
    }

    @Override
    public List<Node> populate(Node source, PopulationContext populationContext) {
        //Node listNode = new Node();
        Node target;
        //Node target = source.getTargetForEdgeThrough(new SubsumesListGuard(listNode));
        SubsumesListGuard existingGuard = source.getGuard(SubsumesListGuard.class);
        Node listNode;
        if (existingGuard != null) {
            listNode = existingGuard.getNode();
            target = source.getTarget(SubsumesListGuard.class);
        } else {
            listNode = new Node();
            target = new Node();
            source.addEdge(new SubsumesListGuard(listNode), target);
        }

        //Node listNodeStart = source.getTargetForEdgeThrough(new SubsumesListGuard());

        ArrayList<Node> targets = new ArrayList<>();
        List<Node> sources = Arrays.asList(listNode);

        if(populators.size() > 0) {
            populate(sources, targets, 0);
            //populate(sources, 0, target);
            //source.addEdge(new SubsumesListGuard(listNode), target);
            //targets.add(target);
        } /*else
            targets.add(source);*/


        return targets;
    }

    @Override
    public Node getTarget(Node source) {
        return null;
    }

    private void populate(List<Node> sources, List<Node> targets, int i) {
        sources.forEach(s -> {
            List<Node> innerTargets = populators.get(i).populate(s, new PopulationContext() {
                @Override
                public NodePopulator getNextPopulator() {
                    return i + 1 < populators.size() ? populators.get(i + 1) : null;
                }

                @Override
                public void addTarget(Node target) {
                    targets.add(target);
                }
            });
            if(i == populators.size() - 1)
                targets.addAll(innerTargets);
            else
                populate(innerTargets, targets, i + 1);
        });
    }
}
