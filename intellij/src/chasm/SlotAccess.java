package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public class SlotAccess extends JsonChangeExpression {
    private JsonChangeExpression target;
    private IdExpression id;

    public SlotAccess(JsonChangeExpression target, IdExpression id) {
        this.target = target;
        this.id = id;
    }

    public JsonChangeExpression getTarget() {
        return target;
    }

    public IdExpression getId() {
        return id;
    }

    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, Object> captures) {
        if(expression instanceof SlotAccess) {
            SlotAccess slotAccess = (SlotAccess)expression;
            return this.target.matches(slotAccess.target, captures) && this.id.matches(slotAccess.id, captures);
        }

        return false;
    }
}
