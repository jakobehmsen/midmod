package chasm;

import java.util.Map;

public class CaptureIdExpression extends IdExpression {
    private String captureId;

    public CaptureIdExpression(String captureId) {
        this.captureId = captureId;
    }

    @Override
    public boolean matches(IdExpression idExpression, Map<String, Object> captures) {
        if(idExpression instanceof SpecificIdExpression) {
            SpecificIdExpression specificIdExpression = (SpecificIdExpression) idExpression;
            captures.put(captureId, new ObjectChangeExpression(specificIdExpression.getId()));
            return true;
        }

        return false;
    }
}
