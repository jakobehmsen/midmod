package chasm;

import java.util.List;
import java.util.Map;

public class TemplateArrayChangeExpression extends ChangeExpression {
    private ChangeExpression itemTemplate;

    public TemplateArrayChangeExpression(ChangeExpression itemTemplate) {
        this.itemTemplate = itemTemplate;
    }

    @Override
    public boolean matches(ChangeExpression expression, Map<String, List<Object>> captures) {
        if(expression instanceof ArrayChangeExpression) {
            ArrayChangeExpression arrayChangeExpression = (ArrayChangeExpression)expression;
            arrayChangeExpression.getItems().forEach(item -> itemTemplate.matches(item, captures));

            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "[" + itemTemplate.toString() + "]";
    }
}
