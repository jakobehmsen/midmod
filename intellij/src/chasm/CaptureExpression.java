package chasm;

import java.util.Map;

/**
 * Created by jakob on 31-07-16.
 */
public class CaptureExpression extends JsonChangeExpression {
    private String captureId;

    public CaptureExpression(String captureId) {
        this.captureId = captureId;
    }

    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, Object> captures) {
        try {
            captures.put(captureId, expression);

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
