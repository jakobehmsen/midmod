package paidia;

import java.util.ArrayList;

public abstract class AbstractValue implements Value {
    private ArrayList<Usage> usages = new ArrayList<>();

    @Override
    public void addUsage(Usage usage) {
        usages.add(usage);
    }

    @Override
    public void removeUsage(Usage usage) {
        usages.remove(usage);
    }

    protected void sendRemoveValue() {
        usages.forEach(x -> x.removeValue());
    }

    protected void sendReplaceValue(Value value) {
        usages.forEach(x -> x.replaceValue(value));
    }
}
