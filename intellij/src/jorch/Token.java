package jorch;

import java.util.function.Consumer;

public interface Token extends AutoCloseable {
    EventChannel getEventChannel();
    void finish(Object result);
    void passTo(Consumer<Token> nextTask);
    Token newToken(Consumer<Token> initialTask);
    Object getResult();
    Token getParent();
}
