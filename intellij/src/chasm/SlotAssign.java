package chasm;

import java.util.List;
import java.util.Map;

public class SlotAssign extends JsonChangeStatement {
    private JsonChangeExpression target;
    private IdExpression id;
    private JsonChangeExpression value;

    public SlotAssign(JsonChangeExpression target, IdExpression id, JsonChangeExpression value) {
        this.target = target;
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean matches(JsonChangeStatement statement, Map<String, List<Object>> captures) {
        if(statement instanceof SlotAssign) {
            SlotAssign slotAssign = (SlotAssign)statement;
            return this.target.matches(slotAssign.target, captures) && this.id.matches(slotAssign.id, captures) &&
                this.value.matches(slotAssign.value, captures);
        }

        return false;
    }

    @Override
    public String toString() {
        return target + "." + id + " = " + value;
    }
}
