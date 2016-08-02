package chasm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObjectLiteralExpression extends JsonChangeExpression {
    public Object get(String slotId) {
        return slots.stream().filter(x -> x.id.equals(slotId)).findFirst().get().value;
    }

    public static class Slot {
        private String id;
        private JsonChangeExpression value;

        public Slot(String id, JsonChangeExpression value) {
            this.id = id;
            this.value = value;
        }
    }

    private List<Slot> slots;

    public ObjectLiteralExpression(List<Slot> slots) {
        this.slots = slots;
    }

    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, List<Object>> captures) {
        if(expression instanceof ObjectLiteralExpression) {
            ObjectLiteralExpression objectLiteralExpression = (ObjectLiteralExpression)expression;

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
}
