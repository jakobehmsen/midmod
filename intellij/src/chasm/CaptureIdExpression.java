package chasm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CaptureIdExpression extends IdChangeExpression {
    private String captureId;

    public CaptureIdExpression(String captureId) {
        this.captureId = captureId;
    }

    @Override
    public boolean matches(IdChangeExpression idExpression, Map<String, List<Object>> captures) {
        if(idExpression instanceof SpecificIdChangeExpression) {
            SpecificIdChangeExpression specificIdExpression = (SpecificIdChangeExpression) idExpression;
            captures.computeIfAbsent(captureId, k -> new ArrayList<>());
            captures.get(captureId).add(new ObjectChangeExpression(specificIdExpression.getId()));
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "@" + captureId;
    }
}
