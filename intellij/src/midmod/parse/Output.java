package midmod.parse;

public interface Output<T> extends TransationSupport {
    void putChar(char ch);
    Output<T> createEmpty();
    Input<T> toInput();

    default void putChars(CharSequence chars) {
        for(int i = 0; i < chars.length(); i++)
            putChar(chars.charAt(i));
    }
}
