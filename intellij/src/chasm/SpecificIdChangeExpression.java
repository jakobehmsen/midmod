package chasm;

import java.util.Map;

public class SpecificIdChangeExpression extends IdChangeExpression {
    private String id;

    public SpecificIdChangeExpression(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean matches(IdChangeExpression idExpression, Map<String, CapturedValue> captures) {
        if(idExpression instanceof SpecificIdChangeExpression) {
            SpecificIdChangeExpression specificIdExpression = (SpecificIdChangeExpression) idExpression;
            return this.id.equals(specificIdExpression.id);
        }

        return false;
    }

    @Override
    public String toString() {
        return id;
    }
}
