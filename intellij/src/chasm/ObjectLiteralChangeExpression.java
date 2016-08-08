package chasm;

import java.util.List;
import java.util.stream.Collectors;

public class ObjectLiteralChangeExpression extends ChangeExpression {
    public Object get(String slotId) {
        return slots.stream().filter(x -> x.id.equals(slotId)).findFirst().get().value;
    }

    public static class Slot {
        private String id;
        private ChangeExpression value;

        public Slot(String id, ChangeExpression value) {
            this.id = id;
            this.value = value;
        }
    }

    private List<Slot> slots;

    public ObjectLiteralChangeExpression(List<Slot> slots) {
        this.slots = slots;
    }

    @Override
    public boolean matches(ChangeExpression expression, Captures captures) {
        if(expression instanceof ObjectLiteralChangeExpression) {
            ObjectLiteralChangeExpression objectLiteralExpression = (ObjectLiteralChangeExpression)expression;

            return this.slots.stream().allMatch(thisSlot -> {
                Slot otherSlot = objectLiteralExpression.slots.stream().filter(x -> x.id.equals(thisSlot.id)).findFirst().orElse(null);
                return otherSlot != null && thisSlot.value.matches(otherSlot.value, captures);
            });
        }

        return false;
    }

    @Override
    public String toString() {
        return "{" + slots.stream().map(x -> x.id + ": " + x.value).collect(Collectors.joining(", ")) + "}";
    }

    @Override
    public Object toValue() {
        return slots.stream().collect(Collectors.toMap(x -> x.id, x -> x.value.toValue()));
    }
}
