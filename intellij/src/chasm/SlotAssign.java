package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public class SlotAssign extends JsonChangeStatement {
    private JsonChangeExpression target;
    private IdExpression id;
    private JsonChangeExpression value;

    public SlotAssign(JsonChangeExpression target, IdExpression id, JsonChangeExpression value) {
        this.target = target;
        this.id = id;
        this.value = value;
    }

    public JsonChangeExpression getTarget() {
        return target;
    }

    public IdExpression getId() {
        return id;
    }

    public JsonChangeExpression getValue() {
        return value;
    }

    @Override
    public boolean matches(JsonChangeStatement statement, Map<String, Object> captures) {
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
