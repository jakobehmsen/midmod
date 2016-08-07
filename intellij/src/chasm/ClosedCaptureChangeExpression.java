package chasm;

import java.util.Hashtable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClosedCaptureChangeExpression extends ChangeExpression {
    private ChangeExpression target;
    private String captureId;
    private Supplier<CapturedValue> capturedValueSupplier;

    public ClosedCaptureChangeExpression(ChangeExpression target, String captureId, Supplier<CapturedValue> capturedValueSupplier) {
        this.target = target;
        this.captureId = captureId;
        this.capturedValueSupplier = capturedValueSupplier;
    }

    @Override
    public boolean matches(ChangeExpression expression, Captures captures) {
        Captures closedCaptures = new Captures();

        if(target.matches(expression, closedCaptures)) {
            ObjectLiteralChangeExpression objectLiteralExpression = new ObjectLiteralChangeExpression(
                closedCaptures.entries().stream().map(x -> new ObjectLiteralChangeExpression.Slot(x.getKey(), (ChangeExpression) x.getValue().buildValue())).collect(Collectors.toList())
            );

            CapturedValue values = captures.computeIfAbsent(captureId, k -> capturedValueSupplier.get());

            values.captureNext(objectLiteralExpression);

            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return target + " @" + captureId;
    }
}
