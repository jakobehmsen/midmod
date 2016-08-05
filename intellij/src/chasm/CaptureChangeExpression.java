package chasm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CaptureChangeExpression extends ChangeExpression {
    private String captureId;
    private Supplier<CapturedValue> capturedValueSupplier;

    public CaptureChangeExpression(String captureId, Supplier<CapturedValue> capturedValueSupplier) {
        this.captureId = captureId;
        this.capturedValueSupplier = capturedValueSupplier;
    }

    @Override
    public boolean matches(ChangeExpression expression, Map<String, CapturedValue> captures) {
        try {
            CapturedValue values = captures.computeIfAbsent(captureId, k -> {
                return capturedValueSupplier.get();

                /*if(isMulti)
                    return new CapturedValue() {
                        List<Object> v = new ArrayList<>();

                        @Override
                        public void captureNext(Object value) {
                            v.add(value);
                        }

                        @Override
                        public Object buildValue() {
                            return v;
                        }
                    };

                return new CapturedValue() {
                    private Object v;

                    @Override
                    public void captureNext(Object value) {
                        v = value;
                    }

                    @Override
                    public Object buildValue() {
                        return v;
                    }
                };*/
            });

            values.captureNext(expression);

            /*List<Object> values = captures.computeIfAbsent(captureId, k -> {
                List<Object> v = new ArrayList<>();
                if(!isMulti)
                    v.add(null);
                return v;
            });

            if(isMulti)
                values.add(expression);
            else
                values.set(0, expression);*/

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
