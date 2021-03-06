package chasm;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ArrayChangeExpression extends ChangeExpression {
    private List<ChangeExpression> items;

    public ArrayChangeExpression(List<ChangeExpression> items) {
        this.items = items;
    }

    public List<ChangeExpression> getItems() {
        return items;
    }

    @Override
    public boolean matches(ChangeExpression expression, Captures captures) {
        if(expression instanceof ArrayChangeExpression) {
            ArrayChangeExpression arrayChangeExpression = (ArrayChangeExpression)expression;
            return
                this.items.size() == arrayChangeExpression.items.size() &&
                IntStream.range(0, items.size()).allMatch(i -> this.items.get(i).matches(arrayChangeExpression.items.get(i), captures));
        }

        return false;
    }

    @Override
    public Object toValue() {
        return items.stream().map(x -> x.toValue()).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return items.toString();
    }
}
