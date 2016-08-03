package chasm;

import java.util.List;
import java.util.Map;

public class SlotAssignChangeExpression extends ChangeStatement {
    private ChangeExpression target;
    private IdChangeExpression id;
    private ChangeExpression value;

    public SlotAssignChangeExpression(ChangeExpression target, IdChangeExpression id, ChangeExpression value) {
        this.target = target;
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean matches(ChangeStatement statement, Map<String, List<Object>> captures) {
        if(statement instanceof SlotAssignChangeExpression) {
            SlotAssignChangeExpression slotAssignChangeExpression = (SlotAssignChangeExpression)statement;
            return this.target.matches(slotAssignChangeExpression.target, captures) && this.id.matches(slotAssignChangeExpression.id, captures) &&
                this.value.matches(slotAssignChangeExpression.value, captures);
        }

        return false;
    }

    @Override
    public String toString() {
        return target + "." + id + " = " + value;
    }
}
