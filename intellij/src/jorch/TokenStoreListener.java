package jorch;

public interface TokenStoreListener {
    void addedToken(TokenStore tokenStore, Token token);
    void removeToken(TokenStore tokenStore, Token token);
}
