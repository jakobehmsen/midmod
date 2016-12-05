package jorch;

import java.util.function.Consumer;

public interface ClassReplacer {
    void replaceWith(Class<? extends Consumer<Token>> c, Object[] arguments);
}
