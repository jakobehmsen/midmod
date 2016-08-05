package chasm;

public interface CapturedValue {
    void captureNext(Object value);
    Object buildValue();
}
