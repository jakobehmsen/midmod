package jorch;

public interface ResponseHandler {
    void onResponse(Object result);
    void onError(Throwable error);
}
