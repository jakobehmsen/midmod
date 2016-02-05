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
    public void populate(Node source, Node target) {
        ArrayList<Node> targets = new ArrayList<>();
        List<Node> sources = Arrays.asList(source);

        if(populators.size() > 0)
            populate(sources, 0, target);
        else {
            // Not supported
        }
    }

    private void populate(List<Node> sources, int i, Node target) {
        sources.forEach(s -> {
            List<Node> innerTargets = populators.get(i).populate(s);

            if(i == populators.size() - 1)
                innerTargets.forEach(x ->
                    x.addEdge(new EndSubsumesListGuard(), target));
                //populators.get(i).populate(s, target);
            else {
                /*List<Node> innerTargets = populators.get(i).populate(s);
                populate(innerTargets, i + 1, target);*/
                populate(innerTargets, i + 1, target);
            }
        });
    }

    @Override
    public List<Node> populate(Node source) {
        //Node listNode = new Node();
        Node target = new Node();
        //Node target = source.getTargetForEdgeThrough(new SubsumesListGuard(listNode));
        Node listNodeStart = source.getTargetForEdgeThrough(new StartSubsumesListGuard());

        ArrayList<Node> targets = new ArrayList<>();
        List<Node> sources = Arrays.asList(listNodeStart);

        if(populators.size() > 0) {
            //populate(sources, targets, 0);
            populate(sources, 0, target);
            //source.addEdge(new SubsumesListGuard(listNode), target);
            targets.add(target);
        } /*else
            targets.add(source);*/


        return targets;
    }

    private void populate(List<Node> sources, List<Node> targets, int i) {
        sources.forEach(s -> {
            List<Node> innerTargets = populators.get(i).populate(s);
            if(i == populators.size() - 1)
                targets.addAll(innerTargets);
            else
                populate(innerTargets, targets, i + 1);
        });
    }
}
