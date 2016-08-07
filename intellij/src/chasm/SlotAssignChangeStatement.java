package chasm;

public class SlotAssignChangeStatement extends ChangeStatement {
    private ChangeExpression target;
    private IdChangeExpression id;
    private ChangeExpression value;

    public SlotAssignChangeStatement(ChangeExpression target, IdChangeExpression id, ChangeExpression value) {
        this.target = target;
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean matches(ChangeStatement statement, Captures captures) {
        if(statement instanceof SlotAssignChangeStatement) {
            SlotAssignChangeStatement slotAssignChangeStatement = (SlotAssignChangeStatement)statement;
            return this.target.matches(slotAssignChangeStatement.target, captures) && this.id.matches(slotAssignChangeStatement.id, captures) &&
                this.value.matches(slotAssignChangeStatement.value, captures);
        }

        return false;
    }

    @Override
    public String toString() {
        return target + "." + id + " = " + value;
    }
}
