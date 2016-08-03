package chasm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CaptureChangeExpression extends ChangeExpression {
    private String captureId;

    public CaptureChangeExpression(String captureId) {
        this.captureId = captureId;
    }

    @Override
    public boolean matches(ChangeExpression expression, Map<String, List<Object>> captures) {
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
