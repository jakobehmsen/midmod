package midmod.rules;

public class AbstractCardinality implements Cardinality {
    private int sizeIndicator;

    public AbstractCardinality(int sizeIndicator) {
        this.sizeIndicator = sizeIndicator;
    }

    @Override
    public int compareTo(Cardinality cardinality) {
        if(cardinality instanceof AbstractCardinality)
            return sizeIndicator - ((AbstractCardinality)cardinality).sizeIndicator;
        return -1;
    }

    @Override
    public Cardinality add(Cardinality cardinality) {
        if(cardinality instanceof AbstractCardinality)
            return new AbstractCardinality(Math.max(sizeIndicator, ((AbstractCardinality)cardinality).sizeIndicator));
        return this;
    }

    @Override
    public Cardinality addTo(ConcreteCardinality cardinality) {
        return this;
    }

    @Override
    public Cardinality addTo(AbstractCardinality cardinality) {
        return new AbstractCardinality(Math.max(sizeIndicator, cardinality.sizeIndicator));
    }

    @Override
    public Cardinality mul(Cardinality cardinality) {
        if(cardinality instanceof AbstractCardinality)
            return new AbstractCardinality(Math.max(sizeIndicator, ((AbstractCardinality)cardinality).sizeIndicator));
        return this;
    }

    @Override
    public Cardinality mulBy(ConcreteCardinality cardinality) {
        return this;
    }

    @Override
    public Cardinality mulBy(AbstractCardinality cardinality) {
        return new AbstractCardinality(Math.max(sizeIndicator, cardinality.sizeIndicator));
    }

    @Override
    public Cardinality negate() {
        return this;
    }
}
