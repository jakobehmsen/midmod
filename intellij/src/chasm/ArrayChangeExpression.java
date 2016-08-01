package chasm;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class ArrayChangeExpression extends JsonChangeExpression {
    private List<JsonChangeExpression> items;

    public ArrayChangeExpression(List<JsonChangeExpression> items) {
        this.items = items;
    }

    public List<JsonChangeExpression> getItems() {
        return items;
    }

    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, List<Object>> captures) {
        if(expression instanceof ArrayChangeExpression) {
            ArrayChangeExpression arrayChangeExpression = (ArrayChangeExpression)expression;
            return
                this.items.size() == arrayChangeExpression.items.size() &&
                IntStream.range(0, items.size()).allMatch(i -> this.items.get(i).matches(arrayChangeExpression.items.get(i), captures));
        }

        return false;
    }
}
