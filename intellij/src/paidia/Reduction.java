package paidia;

public class Reduction extends AbstractValue {
    private Value sourceValue;
    private Value reducedValue;

    public Reduction(Value sourceValue) {
        this.sourceValue = sourceValue;
        this.reducedValue = sourceValue.reduce();

        sourceValue.addUsage(new Usage() {
            @Override
            public void removeValue() {

            }

            @Override
            public void replaceValue(Value value) {
                try {
                    if(Reduction.this.sourceValue != value) {
                        Reduction.this.sourceValue = value;
                        Reduction.this.sourceValue.addUsage(this);
                    }
                    Reduction.this.reducedValue = Reduction.this.sourceValue.reduce();

                    sendReplaceValue(Reduction.this.reducedValue);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public ViewBinding toComponent() {
        return reducedValue.toComponent();
    }

    @Override
    public String toSource() {
        return reducedValue.toSource();
    }

    @Override
    public Value reduce() {
        return reducedValue;
    }
}
