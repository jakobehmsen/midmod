package midmod.rules;

public class ConcreteCardinality implements Cardinality {
    private int value;

    public ConcreteCardinality(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(Cardinality cardinality) {
        if(cardinality instanceof ConcreteCardinality)
            return value - ((ConcreteCardinality)cardinality).value;
        return -1;
    }

    @Override
    public Cardinality add(Cardinality cardinality) {
        return cardinality.addTo(this);
    }

    @Override
    public Cardinality addTo(ConcreteCardinality cardinality) {
        return new ConcreteCardinality(value + cardinality.value);
    }

    @Override
    public Cardinality addTo(AbstractCardinality cardinality) {
        return cardinality;
    }

    @Override
    public Cardinality mul(Cardinality cardinality) {
        return cardinality.mulBy(this);
    }

    @Override
    public Cardinality mulBy(ConcreteCardinality cardinality) {
        return new ConcreteCardinality(value * cardinality.value);
    }

    @Override
    public Cardinality mulBy(AbstractCardinality cardinality) {
        return cardinality;
    }

    @Override
    public Cardinality negate() {
        return new NegatedCardinality(new AbstractCardinality(2), this);
    }
}
