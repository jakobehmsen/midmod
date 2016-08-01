package chasm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CaptureExpression extends JsonChangeExpression {
    private String captureId;

    public CaptureExpression(String captureId) {
        this.captureId = captureId;
    }

    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, List<Object>> captures) {
        try {
            captures.computeIfAbsent(captureId, k -> new ArrayList<>());
            captures.get(captureId).add(expression);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "@" + captureId;
    }
}
