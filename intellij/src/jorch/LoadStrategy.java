package jorch;

import java.util.function.Consumer;

public interface LoadStrategy {
    Consumer<Token> load(Consumer<Token> task);
}
