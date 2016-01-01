package midmod.rules;

public interface Cardinality extends Comparable<Cardinality> {
    Cardinality add(Cardinality cardinality);
    Cardinality addTo(ConcreteCardinality cardinality);
    Cardinality addTo(AbstractCardinality cardinality);
    Cardinality mul(Cardinality cardinality);
    Cardinality mulBy(ConcreteCardinality cardinality);
    Cardinality mulBy(AbstractCardinality cardinality);
    Cardinality negate();
}
