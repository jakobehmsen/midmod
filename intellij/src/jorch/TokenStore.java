package jorch;

import java.util.List;
import java.util.Map;

public interface TokenStore {
    Token newToken(Map<String, Object> context, Step start);
    void removeToken(Token token);
    List<Token> getTokens();
}
