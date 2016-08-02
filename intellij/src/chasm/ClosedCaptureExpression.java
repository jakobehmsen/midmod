package chasm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ClosedCaptureExpression extends JsonChangeExpression {
    private JsonChangeExpression target;
    private String captureId;

    public ClosedCaptureExpression(JsonChangeExpression target, String captureId) {
        this.target = target;
        this.captureId = captureId;
    }

    @Override
    public boolean matches(JsonChangeExpression expression, Map<String, List<Object>> captures) {
        Hashtable<String, List<Object>> closedCaptures = new Hashtable<>();

        if(target.matches(expression, closedCaptures)) {
            ObjectLiteralExpression objectLiteralExpression = new ObjectLiteralExpression(
                closedCaptures.entrySet().stream().map(x -> new ObjectLiteralExpression.Slot(x.getKey(), (JsonChangeExpression) x.getValue().get(0))).collect(Collectors.toList())
            );

            captures.computeIfAbsent(captureId, k -> new ArrayList<>());
            captures.get(captureId).add(objectLiteralExpression);

            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return target + " @" + captureId;
    }
}
