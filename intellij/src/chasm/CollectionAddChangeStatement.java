package chasm;

import java.util.List;
import java.util.Map;

public class CollectionAddChangeStatement extends ChangeStatement {
    private ChangeExpression target;
    private IdChangeExpression id;
    private ChangeExpression value;

    public CollectionAddChangeStatement(ChangeExpression target, IdChangeExpression id, ChangeExpression value) {
        this.target = target;
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean matches(ChangeStatement statement, Map<String, List<Object>> captures) {
        if(statement instanceof CollectionAddChangeStatement) {
            CollectionAddChangeStatement collectionAddChangeStatement = (CollectionAddChangeStatement)statement;
            return this.target.matches(collectionAddChangeStatement.target, captures) && this.id.matches(collectionAddChangeStatement.id, captures) &&
                this.value.matches(collectionAddChangeStatement.value, captures);
        }

        return false;
    }

    @Override
    public String toString() {
        return target + "." + id + " += " + value;
    }
}
