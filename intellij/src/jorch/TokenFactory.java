package jorch;

import java.util.Map;

public interface TokenFactory {
    Token newToken(Map<String, Object> context, Step start);
}
