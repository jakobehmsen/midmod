package midmod.parse;

import java.util.Stack;

public class CharSequenceOutput implements Output<Character> {
    private StringBuilder chars;

    public CharSequenceOutput(StringBuilder chars) {
        this.chars = chars;
    }

    private Stack<Integer> lengths = new Stack<>();

    @Override
    public void putChar(char ch) {
        chars.append(ch);
    }

    @Override
    public Output<Character> createEmpty() {
        return new CharSequenceOutput(new StringBuilder());
    }

    @Override
    public Input<Character> toInput() {
        return new CharSequenceInput(chars);
    }

    @Override
    public void begin() {
        lengths.push(chars.length());
    }

    @Override
    public void commit() {
        lengths.pop();
    }

    @Override
    public void rollback() {
        chars.setLength(lengths.pop());
    }
}
