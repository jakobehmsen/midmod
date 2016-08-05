package chasm;

import java.util.Map;

public class SlotAccessChangeExpression extends ChangeExpression {
    private ChangeExpression target;
    private IdChangeExpression id;

    public SlotAccessChangeExpression(ChangeExpression target, IdChangeExpression id) {
        this.target = target;
        this.id = id;
    }

    @Override
    public boolean matches(ChangeExpression expression, Map<String, CapturedValue> captures) {
        if(expression instanceof SlotAccessChangeExpression) {
            SlotAccessChangeExpression slotAccessChangeExpression = (SlotAccessChangeExpression)expression;
            return this.target.matches(slotAccessChangeExpression.target, captures) && this.id.matches(slotAccessChangeExpression.id, captures);
        }

        return false;
    }

    @Override
    public String toString() {
        return target + "." + id;
    }
}
