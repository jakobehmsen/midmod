package chasm;

import java.util.List;
import java.util.Map;

public class CollectionAddChangeStatement extends JsonChangeStatement {
    private JsonChangeExpression target;
    private IdExpression id;
    private JsonChangeExpression value;

    public CollectionAddChangeStatement(JsonChangeExpression target, IdExpression id, JsonChangeExpression value) {
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
    public boolean matches(JsonChangeStatement statement, Map<String, List<Object>> captures) {
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
