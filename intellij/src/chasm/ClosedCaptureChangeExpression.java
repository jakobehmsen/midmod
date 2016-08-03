package chasm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ClosedCaptureChangeExpression extends ChangeExpression {
    private ChangeExpression target;
    private String captureId;

    public ClosedCaptureChangeExpression(ChangeExpression target, String captureId) {
        this.target = target;
        this.captureId = captureId;
    }

    @Override
    public boolean matches(ChangeExpression expression, Map<String, List<Object>> captures) {
        Hashtable<String, List<Object>> closedCaptures = new Hashtable<>();

        if(target.matches(expression, closedCaptures)) {
            ObjectLiteralChangeExpression objectLiteralExpression = new ObjectLiteralChangeExpression(
                closedCaptures.entrySet().stream().map(x -> new ObjectLiteralChangeExpression.Slot(x.getKey(), (ChangeExpression) x.getValue().get(0))).collect(Collectors.toList())
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
