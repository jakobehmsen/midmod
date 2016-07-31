package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public class SpecificIdExpression extends IdExpression {
    private String id;

    public SpecificIdExpression(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean matches(IdExpression idExpression, Map<String, Object> captures) {
        if(idExpression instanceof SpecificIdExpression) {
            SpecificIdExpression specificIdExpression = (SpecificIdExpression) idExpression;
            return this.id.equals(specificIdExpression.id);
        }

        return false;
    }

    @Override
    public String toString() {
        return id;
    }
}
