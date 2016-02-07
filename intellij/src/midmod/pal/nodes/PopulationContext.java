package midmod.pal.nodes;

public interface PopulationContext {

    NodePopulator getNextPopulator();

    void addTarget(Node target);
}
