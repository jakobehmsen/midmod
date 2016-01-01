package midmod.rules;

public class NegatedCardinality implements Cardinality {
    private Cardinality cardinality;
    private Cardinality source;

    public NegatedCardinality(Cardinality cardinality, Cardinality source) {
        this.cardinality = cardinality;
        this.source = source;
    }

    @Override
    public Cardinality add(Cardinality cardinality) {
        return this.cardinality.add(cardinality);
    }

    @Override
    public Cardinality addTo(ConcreteCardinality cardinality) {
        return this.cardinality.addTo(cardinality);
    }

    @Override
    public Cardinality addTo(AbstractCardinality cardinality) {
        return this.cardinality.addTo(cardinality);
    }

    @Override
    public Cardinality mul(Cardinality cardinality) {
        return this.cardinality.mul(cardinality);
    }

    @Override
    public Cardinality mulBy(ConcreteCardinality cardinality) {
        return this.cardinality.mulBy(cardinality);
    }

    @Override
    public Cardinality mulBy(AbstractCardinality cardinality) {
        return this.cardinality.mulBy(cardinality);
    }

    @Override
    public Cardinality negate() {
        return source;
    }

    @Override
    public int compareTo(Cardinality o) {
        return cardinality.compareTo(o);
    }
}
