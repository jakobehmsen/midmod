package chasm;

import java.util.function.Supplier;

public class CaptureIdExpression extends IdChangeExpression {
    private String captureId;
    private Supplier<CapturedValue> capturedValueSupplier;

    public CaptureIdExpression(String captureId, Supplier<CapturedValue> capturedValueSupplier) {
        this.captureId = captureId;
        this.capturedValueSupplier = capturedValueSupplier;
    }

    @Override
    public boolean matches(IdChangeExpression idExpression, Captures captures) {
        if(idExpression instanceof SpecificIdChangeExpression) {
            SpecificIdChangeExpression specificIdExpression = (SpecificIdChangeExpression) idExpression;
            captures.computeIfAbsent(captureId, k -> capturedValueSupplier.get());
            captures.get(captureId).captureNext(new ObjectChangeExpression(specificIdExpression.getId()));
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "@" + captureId;
    }
}
